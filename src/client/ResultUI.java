package client;
import javax.swing.*;

public class ResultUI {
    public ResultUI(int score) {
        JFrame frame = new JFrame("Result");
        frame.setSize(300,200);

        JLabel label = new JLabel("Your Score: " + score, JLabel.CENTER);
        frame.add(label);

        frame.setVisible(true);
    }
}