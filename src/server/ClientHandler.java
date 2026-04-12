package server;

import java.io.*;
import java.net.*;
import java.util.*;
import model.Question;

/**
 * Handles one connected client.
 *
 * Lobby phase : waits on QuizServer.startLatch (released by QuizServer once
 *               MIN_PLAYERS have joined and the countdown has fired).
 * Quiz phase  : sends questions, receives answers, scores, saves, broadcasts.
 */
public class ClientHandler extends Thread {

    private final Socket socket;
    private BufferedReader in;
    private PrintWriter    out;

    private String playerName  = "Unknown";
    private int    score       = 0;
    private boolean scoreSaved = false; // guard: save exactly once even on disconnect

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("[ClientHandler] Stream init error: " + e.getMessage());
        }
    }

    /** Thread-safe send used by QuizServer.broadcastAll(). */
    public void send(String message) {
        if (out != null) out.println(message);
    }

    @Override
    public void run() {
        try {
            // ── 1. Read player name ───────────────────────────────────────────
            playerName = in.readLine();
            if (playerName == null || playerName.trim().isEmpty()) playerName = "Anonymous";
            playerName = playerName.trim();
            System.out.println("[Handler] Player connected: " + playerName);

            // ── 2. Send current leaderboard immediately so client sees it now ──
            send("LEADERBOARD");
            send(QuizManager.getLeaderboard());

            // ── 3. Lobby wait ─────────────────────────────────────────────────
            QuizServer.startLatch.await();

            // ── 4. Load questions ─────────────────────────────────────────────
            List<Question> questions = QuizManager.getQuestions();

            if (questions.isEmpty()) {
                send("ERROR:No questions found. Contact admin.");
                return;
            }

            // ── 5. Quiz loop ──────────────────────────────────────────────────
            for (Question q : questions) {
                sendQuestion(q);

                String raw = in.readLine();
                if (raw == null) {
                    System.out.println("[Handler] " + playerName + " disconnected mid-quiz.");
                    return; // finally block will save the partial score
                }

                int answer;
                try { answer = Integer.parseInt(raw.trim()); }
                catch (NumberFormatException e) { answer = -1; }

                if (answer >= 0 && answer == q.getCorrectAnswer()) {
                    score++;
                    System.out.println("[Handler] " + playerName + " ✔ correct (Q id=" + q.getId() + ")");
                } else {
                    System.out.println("[Handler] " + playerName
                        + " ✘ wrong – answered " + answer
                        + ", correct=" + q.getCorrectAnswer());
                }
            }

            // ── 6. Save score + send final results ────────────────────────────
            saveScoreOnce();

            send("LEADERBOARD");
            send(QuizManager.getLeaderboard());
            send("FINAL_SCORE:" + score);

            System.out.println("[Handler] " + playerName + " finished. Score=" + score);

        } catch (IOException | InterruptedException e) {
            System.err.println("[Handler] Error for " + playerName + ": " + e.getMessage());
        } finally {
            // Always save score here – catches mid-game disconnects
            saveScoreOnce();
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Saves score exactly once. Called both at normal end AND in the finally
     * block so mid-game disconnects are always recorded.
     */
    private void saveScoreOnce() {
        if (!scoreSaved) {
            scoreSaved = true;
            QuizManager.saveScore(playerName, score);
        }
    }

    /**
     * Shuffles the 4 options, updates q.correctAnswer to the new position,
     * then sends question text + shuffled options to the client.
     */
    private void sendQuestion(Question q) {
        String[] opts = q.getOptions().clone();

        List<Integer> indices = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(indices);

        int newCorrectPos = indices.indexOf(q.getCorrectAnswer());
        q.setCorrectAnswer(newCorrectPos);

        out.println(q.getQuestionText());
        for (int shuffledPos : indices) out.println(opts[shuffledPos]);
    }
}