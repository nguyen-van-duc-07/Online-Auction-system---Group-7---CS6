package com.auction.server.repository;

import com.auction.server.config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserRepository {

  // 1. Hàm lấy mật khẩu để Đăng nhập
  public String getPasswordByAccountName(String username) {
    // Lưu ý: Đổi account_name thành username cho khớp với phpMyAdmin của bạn
    String sql = "SELECT password FROM users WHERE username = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getString("password");
        }
      }
    } catch (Exception e) {
      System.err.println("Lỗi khi lấy password: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  // 2. Hàm lưu người dùng mới vào Database khi Đăng ký
  public boolean saveUser(String username, String hashedPassword, String fullName, double balance) {
    String sql = "INSERT INTO users (username, password, full_name, balance) VALUES (?, ?, ?, ?)";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, username);
      ps.setString(2, hashedPassword);
      ps.setString(3, fullName);
      ps.setDouble(4, balance);

      int rowsAffected = ps.executeUpdate();
      return rowsAffected > 0; // Trả về true nếu chèn thành công

    } catch (Exception e) {
      System.err.println("Lỗi khi lưu người dùng: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }
}