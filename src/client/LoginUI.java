package client;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point for the quiz client.
 * Shows name + server address inputs, then launches QuizUI.
 */
public class LoginUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            // Name
            panel.add(new JLabel("Your name:"));
            JTextField nameField = new JTextField("Player1", 18);
            panel.add(nameField);

            // Server host  (pre-filled with localhost for local testing)
            panel.add(new JLabel("Server host:"));
            JTextField hostField = new JTextField("localhost", 18);
            panel.add(hostField);

            // Port
            panel.add(new JLabel("Port:"));
            JTextField portField = new JTextField("5000", 18);
            panel.add(portField);

            int result = JOptionPane.showConfirmDialog(
                null, panel,
                "Multiplayer Quiz – Connect",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) return;

            String name = nameField.getText().trim();
            String host = hostField.getText().trim();
            String portStr = portField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (host.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Server host cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int port;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Port must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new QuizUI(name, host, port);
        });
    }
}