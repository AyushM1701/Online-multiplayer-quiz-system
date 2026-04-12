package client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Main quiz window.
 *
 * Server protocol expected:
 *   Per question  → question text, then 4 option lines
 *   Leaderboard   → "LEADERBOARD" line, then one pipe-delimited line
 *   End of quiz   → "FINAL_SCORE:<n>"
 */
public class QuizUI extends JFrame {

    // ── UI components ─────────────────────────────────────────────────────────
    // Initialized here so every constructor code-path (including early return)
    // satisfies Java's "definitely assigned" rule for final fields.
    private final JLabel     questionLabel   = new JLabel("Connecting to server…");
    private final JButton[]  optionButtons   = new JButton[4];
    private final JLabel     timerLabel      = new JLabel("⏱ 10");
    private final JTextArea  leaderboardArea = new JTextArea();

    // ── State ─────────────────────────────────────────────────────────────────
    private final ClientConnection connection;
    private final String playerName;         // kept for restart
    private final String serverHost;         // kept for restart
    private final int    serverPort;         // kept for restart
    private Timer   swingTimer;
    private int     timeLeft   = 10;
    private boolean answerSent = false;      // guard: send only one answer per question

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG_DARK   = new Color(25,  25,  35);
    private static final Color BG_MID    = new Color(40,  40,  55);
    private static final Color ACCENT    = new Color(99,  179, 237);
    private static final Color BTN_NORM  = new Color(60,  100, 160);
    private static final Color BTN_HOVER = new Color(80,  130, 200);
    private static final Color C_CORRECT = new Color(50,  200, 100);
    private static final Color C_WRONG   = new Color(220,  70,  70);
    private static final Color C_TIMEOUT = new Color(180, 180,  60);

    // ─────────────────────────────────────────────────────────────────────────
    /** Convenience constructor – uses localhost:5000 */
    public QuizUI(String playerName) {
        this(playerName, ClientConnection.DEFAULT_HOST, ClientConnection.DEFAULT_PORT);
    }

    public QuizUI(String playerName, String host, int port) {
        this.playerName = playerName;
        this.serverHost = host;
        this.serverPort = port;
        connection = new ClientConnection(playerName, host, port);

        if (!connection.isConnected()) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to server at " + host + ":" + port +
                "\nMake sure QuizServer is running.",
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        setTitle("Quiz — " + playerName + "  [" + host + ":" + port + "]");
        setSize(720, 460);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(10, 10));

        // ── Top panel (question + timer) ──────────────────────────────────────
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(BG_MID);
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        questionLabel.setForeground(Color.WHITE);
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));

        timerLabel.setForeground(ACCENT);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        topPanel.add(questionLabel, BorderLayout.CENTER);
        topPanel.add(timerLabel,    BorderLayout.EAST);

        // ── Options panel ─────────────────────────────────────────────────────
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        optionsPanel.setBackground(BG_DARK);
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 8));

        for (int i = 0; i < 4; i++) {
            JButton btn = new JButton();
            btn.setBackground(BTN_NORM);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
            btn.setEnabled(false); // disabled until question arrives

            // Hover effect
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (btn.isEnabled()) btn.setBackground(BTN_HOVER);
                }
                public void mouseExited(MouseEvent e) {
                    if (btn.isEnabled() && btn.getBackground() != C_CORRECT
                            && btn.getBackground() != C_WRONG) {
                        btn.setBackground(BTN_NORM);
                    }
                }
            });

            int idx = i;
            btn.addActionListener(e -> submitAnswer(idx));

            optionButtons[i] = btn;
            optionsPanel.add(btn);
        }

        // ── Leaderboard panel ─────────────────────────────────────────────────
        leaderboardArea.setEditable(false);
        leaderboardArea.setBackground(new Color(18, 18, 28));
        leaderboardArea.setForeground(new Color(100, 220, 130));
        leaderboardArea.setFont(new Font("Consolas", Font.BOLD, 13));
        leaderboardArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        leaderboardArea.setText("Loading scores…"); // shown until server responds

        JScrollPane scroll = new JScrollPane(leaderboardArea);
        scroll.setPreferredSize(new Dimension(230, 0));
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            "🏆 Leaderboard",
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            ACCENT));

        // ── Assemble frame ────────────────────────────────────────────────────
        add(topPanel,     BorderLayout.NORTH);
        add(optionsPanel, BorderLayout.CENTER);
        add(scroll,       BorderLayout.EAST);

        setVisible(true);

        // ── Background thread: receive data from server ───────────────────────
        Thread networkThread = new Thread(this::receiveLoop, "NetworkThread");
        networkThread.setDaemon(true);
        networkThread.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Network receive loop
    // ─────────────────────────────────────────────────────────────────────────
    private void receiveLoop() {
        try {
            String line;
            while ((line = connection.in.readLine()) != null) {
                final String msg = line;

                // ── LOBBY waiting room ────────────────────────────────────────
                if (msg.startsWith("LOBBY_MSG:")) {
                    String lobbyText = msg.substring(10);
                    SwingUtilities.invokeLater(() -> {
                        questionLabel.setText("⏳  " + lobbyText);
                        timerLabel.setText("");
                        setButtonsEnabled(false);
                    });
                    continue;
                }

                // ── LEADERBOARD ───────────────────────────────────────────────
                if (msg.equals("LEADERBOARD")) {
                    String raw = connection.in.readLine(); // single pipe-delimited line
                    // Convert pipe-delimited entries → newlines for display
                    String board = (raw != null) ? raw.replace("|", "\n") : "";
                    SwingUtilities.invokeLater(() -> leaderboardArea.setText(board));
                    continue;
                }

                // ── FINAL_SCORE ───────────────────────────────────────────────
                if (msg.startsWith("FINAL_SCORE:")) {
                    stopTimer();
                    String score = msg.split(":")[1];
                    SwingUtilities.invokeLater(() -> showFinalScore(score));
                    continue;
                }

                // ── ERROR from server ─────────────────────────────────────────
                if (msg.startsWith("ERROR:")) {
                    SwingUtilities.invokeLater(() -> {
                        questionLabel.setText("⚠ " + msg.substring(6));
                        setButtonsEnabled(false);
                    });
                    continue;
                }

                // ── QUESTION (first line already read as `msg`) ───────────────
                String questionText = msg;
                String[] opts = new String[4];
                for (int i = 0; i < 4; i++) {
                    opts[i] = connection.in.readLine();
                }

                final String[] finalOpts = opts;
                SwingUtilities.invokeLater(() -> {
                    questionLabel.setText(questionText);
                    for (int i = 0; i < 4; i++) {
                        optionButtons[i].setText(finalOpts[i]);
                        optionButtons[i].setBackground(BTN_NORM);
                        optionButtons[i].setEnabled(true);
                    }
                    answerSent = false;
                    startTimer();
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() ->
                questionLabel.setText("❌ Connection lost."));
        } finally {
            connection.close();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Submit answer (also called by timer on timeout)
    // ─────────────────────────────────────────────────────────────────────────
    private void submitAnswer(int index) {
        if (answerSent) return; // prevent duplicate sends
        answerSent = true;
        stopTimer();
        setButtonsEnabled(false);

        if (index >= 0) {
            optionButtons[index].setBackground(BTN_HOVER); // brief visual feedback
        }

        connection.out.println(index); // -1 = timed out
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Timer
    // ─────────────────────────────────────────────────────────────────────────
    private void startTimer() {
        stopTimer();
        timeLeft = 10;
        timerLabel.setForeground(ACCENT);
        timerLabel.setText("⏱ " + timeLeft);

        swingTimer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("⏱ " + timeLeft);

            if (timeLeft <= 3) timerLabel.setForeground(C_WRONG);

            if (timeLeft <= 0) {
                timerLabel.setForeground(C_TIMEOUT);
                timerLabel.setText("⏱ Time's up!");
                submitAnswer(-1); // timeout → wrong
            }
        });
        swingTimer.start();
    }

    private void stopTimer() {
        if (swingTimer != null && swingTimer.isRunning()) {
            swingTimer.stop();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private void setButtonsEnabled(boolean enabled) {
        for (JButton btn : optionButtons) btn.setEnabled(enabled);
    }

    private void showFinalScore(String score) {
        stopTimer();
        setButtonsEnabled(false);
        timerLabel.setText("");

        int s = Integer.parseInt(score);

        // ── Result message ────────────────────────────────────────────────────
        String emoji = s == 0 ? "😢" : s <= 2 ? "👍" : s <= 5 ? "🌟" : "🏆";
        String msg   = s == 0 ? "Better luck next time!"
                     : s <= 2 ? "Good effort!"
                     : s <= 5 ? "Well done!"
                              : "Outstanding!";
        questionLabel.setText(emoji + "  Final Score: " + score + "   —   " + msg);

        // ── Replace option buttons with result panel ───────────────────────────
        // Find the CENTER panel (optionsPanel) and swap it out
        Container contentPane = getContentPane();

        // Build result panel
        JPanel resultPanel = new JPanel(new GridBagLayout());
        resultPanel.setBackground(BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 20, 14, 20);
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.gridx  = 0;

        // Score display
        JLabel scoreLabel = new JLabel("Your Score: " + score, JLabel.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        scoreLabel.setForeground(s >= 3 ? new Color(80, 200, 120) : new Color(220, 90, 90));
        gbc.gridy = 0;
        resultPanel.add(scoreLabel, gbc);

        // Message
        JLabel msgLabel = new JLabel(emoji + "  " + msg, JLabel.CENTER);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        msgLabel.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 1;
        resultPanel.add(msgLabel, gbc);

        // ── Play Again button ─────────────────────────────────────────────────
        JButton playAgainBtn = new JButton("▶  Play Again");
        playAgainBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        playAgainBtn.setBackground(new Color(50, 160, 90));
        playAgainBtn.setForeground(Color.WHITE);
        playAgainBtn.setFocusPainted(false);
        playAgainBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playAgainBtn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        playAgainBtn.addActionListener(e -> restartQuiz());
        gbc.gridy = 2;
        resultPanel.add(playAgainBtn, gbc);

        // ── Quit button ───────────────────────────────────────────────────────
        JButton quitBtn = new JButton("✖  Quit");
        quitBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        quitBtn.setBackground(new Color(160, 50, 50));
        quitBtn.setForeground(Color.WHITE);
        quitBtn.setFocusPainted(false);
        quitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        quitBtn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        quitBtn.addActionListener(e -> System.exit(0));
        gbc.gridy = 3;
        resultPanel.add(quitBtn, gbc);

        // Swap CENTER panel
        contentPane.remove(1);          // index 1 = CENTER (options panel)
        contentPane.add(resultPanel, BorderLayout.CENTER);
        contentPane.revalidate();
        contentPane.repaint();
    }

    // ── Restart ───────────────────────────────────────────────────────────────
    private void restartQuiz() {
        connection.close();
        dispose();
        new QuizUI(playerName, serverHost, serverPort);
    }
}