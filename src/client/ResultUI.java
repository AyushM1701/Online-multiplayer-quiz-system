package client;

import java.awt.*;
import javax.swing.*;

/**
 * Pops up a small result window showing the player's final score.
 */
public class ResultUI extends JFrame {

    public ResultUI(int score) {
        setTitle("Quiz Result");
        setSize(320, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(new Color(25, 25, 35));
        setLayout(new BorderLayout(10, 10));

        // Score label
        JLabel scoreLabel = new JLabel("🎯 Your Score: " + score, JLabel.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        scoreLabel.setForeground(score >= 2 ? new Color(80, 200, 120) : new Color(220, 80, 80));

        // Message
        String msg = score == 0 ? "Better luck next time!"
                   : score == 1 ? "Keep practising!"
                   : score <= 2 ? "Good job! 👍"
                                : "Excellent! 🏆";
        JLabel msgLabel = new JLabel(msg, JLabel.CENTER);
        msgLabel.setForeground(Color.LIGHT_GRAY);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(new Color(60, 100, 160));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());

        JPanel bottom = new JPanel();
        bottom.setBackground(new Color(25, 25, 35));
        bottom.add(closeBtn);

        add(scoreLabel, BorderLayout.CENTER);
        add(msgLabel,   BorderLayout.NORTH);
        add(bottom,     BorderLayout.SOUTH);

        setVisible(true);
    }
}