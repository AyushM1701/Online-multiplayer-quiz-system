package client;

import java.awt.*;
import javax.swing.*;

public class QuizUI {
    ClientConnection connection;

    public QuizUI(String name) {
        connection = new ClientConnection(name);

        JFrame frame = new JFrame("Quiz - " + name);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        JLabel questionLabel = new JLabel("Waiting for question...");
        JPanel panel = new JPanel(new GridLayout(4, 1));

        JButton[] buttons = new JButton[4];

        for (int i = 0; i < 4; i++) {
            buttons[i] = new JButton();
            panel.add(buttons[i]);

            int index = i;
            buttons[i].addActionListener(e -> {
                connection.out.println(index); // send answer
            });
        }

        frame.add(questionLabel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);

        frame.setVisible(true);

        // 🔥 Thread to receive data from server
        new Thread(() -> {
            try {
                while (true) {
                    String q = connection.in.readLine();

                    if (q.startsWith("FINAL_SCORE")) {
                        JOptionPane.showMessageDialog(frame, "Score: " + q.split(":")[1]);
                        frame.dispose();
                        break;
                    }

                    questionLabel.setText(q);

                    for (int i = 0; i < 4; i++) {
                        buttons[i].setText(connection.in.readLine());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}