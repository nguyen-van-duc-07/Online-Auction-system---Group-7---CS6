package repository;

import config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserRepository {

    public String getPasswordByAccountName(String accountName) {
        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT password FROM users WHERE account_name = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, accountName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("password");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}