package client;

import java.io.*;
import java.net.*;

public class ClientConnection {
    public Socket socket;
    public BufferedReader in;
    public PrintWriter out;

    public ClientConnection(String name) {
        try {
            socket = new Socket("localhost", 5000);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(name); // send player name

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}