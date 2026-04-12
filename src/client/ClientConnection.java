package client;

import java.io.*;
import java.net.*;

/**
 * Opens a TCP connection to the quiz server and exposes buffered IO streams.
 */
public class ClientConnection {

    public static final String DEFAULT_HOST = "localhost";
    public static final int    DEFAULT_PORT = 5000;

    public Socket       socket;
    public BufferedReader in;
    public PrintWriter    out;

    private boolean connected = false;

    /**
     * Connect with default host/port.
     */
    public ClientConnection(String playerName) {
        this(playerName, DEFAULT_HOST, DEFAULT_PORT);
    }

    /**
     * Connect to a custom host/port (useful for LAN / remote play).
     */
    public ClientConnection(String playerName, String host, int port) {
        try {
            socket = new Socket(host, port);
            in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out    = new PrintWriter(socket.getOutputStream(), true);

            out.println(playerName); // first message: announce name
            connected = true;
            System.out.println("[Client] Connected to " + host + ":" + port + " as '" + playerName + "'");

        } catch (ConnectException e) {
            System.err.println("[Client] ❌ Cannot connect to server at "
                    + host + ":" + port + ". Is the server running?");
        } catch (IOException e) {
            System.err.println("[Client] Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isConnected() { return connected && socket != null && !socket.isClosed(); }

    public void close() {
        try { if (socket != null) socket.close(); }
        catch (IOException ignored) {}
    }
}