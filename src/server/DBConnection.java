package server;

import java.sql.*;

public class DBConnection {
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/quiz_db",
                "root",
                "ayush2006" // CHANGE THIS
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}