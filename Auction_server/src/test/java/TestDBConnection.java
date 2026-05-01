import config.DBConnection;

import java.sql.Connection;

public class TestDBConnection {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ CONNECT SUCCESS to database!");
            } else {
                System.out.println("❌ CONNECT FAILED!");
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR when connecting:");
            e.printStackTrace();
        }
    }
}