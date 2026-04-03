package server;

import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private int score = 0;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

public void run() {
    try {
        String name = in.readLine();
        System.out.println("Player joined: " + name);

        List<String[]> questions = QuizManager.getQuestions();

        for (String[] q : questions) {
            // Send question
            out.println(q[0]);
            for (int i = 1; i <= 4; i++) {
                out.println(q[i]);
            }

            // Receive answer
            int answer = Integer.parseInt(in.readLine());

            if (answer == Integer.parseInt(q[5])) {
                score++;
            }
        }

        // ✅ Save score AFTER quiz ends
        saveScore(name, score);

        // Send final score to client
        out.println("FINAL_SCORE:" + score);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void saveScore(String name, int score) {
    try {
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO scores(username, score) VALUES (?, ?)"
        );

        ps.setString(1, name);
        ps.setInt(2, score);

        ps.executeUpdate();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}