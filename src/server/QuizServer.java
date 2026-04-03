package server;

import java.net.*;
import java.util.*;

public class QuizServer {
    public static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(5000);
            System.out.println("✅ Server Started on port 5000...");

            while (true) {
                Socket socket = server.accept();
                System.out.println("New client connected!");

                ClientHandler client = new ClientHandler(socket);
                clients.add(client);
                client.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}