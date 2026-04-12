package server;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Quiz Server with a synchronized lobby.
 *
 * All clients wait until MIN_PLAYERS have joined, then a countdown fires
 * and everyone starts simultaneously. After the round, the lobby resets.
 *
 * Usage:
 *   java -cp "lib/*:out" server.QuizServer          (requires 2 players)
 *   java -cp "lib/*:out" server.QuizServer 3        (requires 3 players)
 */
public class QuizServer {

    public static final int PORT = 5000;

    /** How many players must join before the quiz begins. */
    public static int MIN_PLAYERS = 2;

    /**
     * CountDownLatch shared by all ClientHandlers.
     * Each handler calls await() after the lobby; server calls countDown()
     * MIN_PLAYERS times to release them all at the same instant.
     */
    public static CountDownLatch startLatch;

    /** All connected handlers for this round (thread-safe). */
    public static final List<ClientHandler> clients =
            Collections.synchronizedList(new ArrayList<>());

    // Keep one ServerSocket alive across rounds so the port stays open.
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        if (args.length > 0) {
            try { MIN_PLAYERS = Integer.parseInt(args[0]); }
            catch (NumberFormatException ignored) {}
        }

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   Multiplayer Quiz Server v2.0       ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("Min players to start : " + MIN_PLAYERS);
        System.out.println("Port                 : " + PORT + "\n");

        if (DBConnection.getConnection() == null) {
            System.err.println("❌ Cannot reach database. Exiting.");
            return;
        }
        System.out.println("✅ Database connection OK\n");

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            System.out.println("[Server] Shutting down.")));

        try {
            serverSocket = new ServerSocket(PORT);
            runLobbyLoop();
        } catch (IOException | InterruptedException e) {
            System.err.println("[Server] Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Outer loop: each iteration = one full quiz round. */
    private static void runLobbyLoop() throws IOException, InterruptedException {
        while (true) {
            // ── Reset for new round ───────────────────────────────────────────
            clients.clear();
            startLatch = new CountDownLatch(MIN_PLAYERS);
            System.out.println("[Lobby] Waiting for " + MIN_PLAYERS + " player(s) …");

            // ── Accept players until lobby is full ────────────────────────────
            while (clients.size() < MIN_PLAYERS) {
                Socket socket = serverSocket.accept();
                System.out.println("[Lobby] New connection: "
                        + socket.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                handler.setDaemon(true);
                handler.start();

                int joined = clients.size();
                System.out.println("[Lobby] " + joined + "/" + MIN_PLAYERS + " players joined.");
                broadcastLobbyStatus(joined);
            }

            // ── Countdown ─────────────────────────────────────────────────────
            broadcastAll("LOBBY_MSG:All players ready! Starting in 3 …");
            Thread.sleep(1000);
            broadcastAll("LOBBY_MSG:2 …");
            Thread.sleep(1000);
            broadcastAll("LOBBY_MSG:1 …");
            Thread.sleep(1000);
            broadcastAll("LOBBY_MSG:GO!");

            // Release all ClientHandler threads simultaneously
            for (int i = 0; i < MIN_PLAYERS; i++) startLatch.countDown();

            // ── Wait for round to finish ──────────────────────────────────────
            for (ClientHandler c : new ArrayList<>(clients)) {
                try { c.join(); } catch (InterruptedException ignored) {}
            }

            System.out.println("\n[Server] Round complete. Restarting lobby …\n");
        }
    }

    /** Sends a message to every currently connected client. */
    public static void broadcastAll(String message) {
        synchronized (clients) {
            for (ClientHandler c : clients) c.send(message);
        }
    }

    private static void broadcastLobbyStatus(int joined) {
        broadcastAll("LOBBY_MSG:Lobby: " + joined + "/" + MIN_PLAYERS
                   + " player(s) connected …");
    }
}