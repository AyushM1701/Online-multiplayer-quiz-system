package server;

import java.sql.*;
import java.util.*;
import model.Question;

/**
 * Loads questions from MySQL and builds the leaderboard string.
 *
 * IMPORTANT – leaderboard protocol:
 *   The leaderboard is sent as ONE socket line with rows joined by "|"
 *   so that readLine() on the client receives the complete data in one call.
 *   The client replaces "|" with newlines for display.
 */
public class QuizManager {

    // ── Questions ─────────────────────────────────────────────────────────────

    public static List<Question> getQuestions() {
        List<Question> list = new ArrayList<>();

        Connection con = DBConnection.getConnection();
        if (con == null) {
            System.err.println("[QuizManager] No DB connection – cannot load questions.");
            return list;
        }

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM questions ORDER BY RAND()")) {

            while (rs.next()) {
                int    id     = rs.getInt("id");
                String text   = rs.getString("question");
                String[] opts = {
                    rs.getString("option1"),
                    rs.getString("option2"),
                    rs.getString("option3"),
                    rs.getString("option4")
                };
                int answer = rs.getInt("answer"); // 0-indexed
                list.add(new Question(id, text, opts, answer));
            }

            System.out.println("[QuizManager] Loaded " + list.size() + " questions.");

        } catch (SQLException e) {
            System.err.println("[QuizManager] Error loading questions: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }

        return list;
    }

    // ── Leaderboard ───────────────────────────────────────────────────────────

    public static String getLeaderboard() {
        Connection con = DBConnection.getConnection();
        if (con == null) return "Leaderboard unavailable";

        StringBuilder sb = new StringBuilder();
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT username, score FROM scores ORDER BY score DESC LIMIT 5")) {

            int rank = 1;
            while (rs.next()) {
                if (sb.length() > 0) sb.append("|");
                sb.append(rank++)
                  .append(". ")
                  .append(rs.getString("username"))
                  .append("  →  ")
                  .append(rs.getInt("score"));
            }

        } catch (SQLException e) {
            System.err.println("[QuizManager] Leaderboard query failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }

        return sb.length() > 0 ? sb.toString() : "No scores yet";
    }

    // ── Score persistence ─────────────────────────────────────────────────────

    public static void saveScore(String username, int score) {
        Connection con = DBConnection.getConnection();
        if (con == null) {
            System.err.println("[QuizManager] DB unavailable – score not saved.");
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO scores(username, score) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setInt(2, score);
            ps.executeUpdate();
            System.out.println("[QuizManager] Saved: " + username + " → " + score);
        } catch (SQLException e) {
            System.err.println("[QuizManager] DB save failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }
    }
}