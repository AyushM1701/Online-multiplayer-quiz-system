package server;

import java.sql.*;

/**
 * Provides a fresh JDBC connection to MySQL.
 *
 * Set the DB password via:
 *   1. System property   -DDB_PASS=yourpassword      (recommended)
 *   2. Environment var   DB_PASS=yourpassword
 *   3. Fall-back literal below (change before production use)
 */
public class DBConnection {

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB   = "quiz_db";
    private static final String USER = "root";

    // ── Resolve password at class-load time ───────────────────────────────────
    private static final String PASS;
    static {
        String p = System.getProperty("DB_PASS");
        if (p == null) p = System.getenv("DB_PASS");
        if (p == null) p = "ayush2006";   // ← change this default
        PASS = p;

        // Force driver registration (needed when running outside a container)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL driver not found on classpath!");
            e.printStackTrace();
        }
    }

    /**
     * Returns a new Connection, or null if the connection cannot be established.
     * Callers must close the connection themselves.
     */
    public static Connection getConnection() {
        String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB
                   + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try {
            return DriverManager.getConnection(url, USER, PASS);
        } catch (SQLException e) {
            System.err.println("[DBConnection] Cannot connect to database: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}