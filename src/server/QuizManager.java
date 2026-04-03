package server;

import java.sql.*;
import java.util.*;

public class QuizManager {

    public static List<String[]> getQuestions() {
        List<String[]> list = new ArrayList<>();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM questions");

            while (rs.next()) {
                String[] q = new String[6];

                q[0] = rs.getString("question");
                q[1] = rs.getString("option1");
                q[2] = rs.getString("option2");
                q[3] = rs.getString("option3");
                q[4] = rs.getString("option4");
                q[5] = String.valueOf(rs.getInt("answer"));

                list.add(q);
                System.out.println("Questions loaded: " + list.size());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}