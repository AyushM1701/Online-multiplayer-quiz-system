package client;

import javax.swing.*;

public class LoginUI {
    public static void main(String[] args) {
        String name = JOptionPane.showInputDialog("Enter your name:");

        if (name != null && !name.isEmpty()) {
            new QuizUI(name);
        }
    }
}