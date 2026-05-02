package com.auction.server.config;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Class được sử dụng để tạo kết nối tới Database nội bộ (XAMPP).
 */
public class DBConnection {
  // localhost:3306 là địa chỉ mặc định của XAMPP
  // auction_db là tên Database bạn vừa tạo trong phpMyAdmin
  private static final String URL = "jdbc:mysql://localhost:3306/auction_db";

  // XAMPP mặc định user là root và không có mật khẩu
  private static final String USER = "root";
  private static final String PASS = "";

  public static Connection getConnection() throws Exception {
    // Nạp driver (tùy phiên bản JDBC, dòng này có thể không bắt buộc nhưng nên có)
    Class.forName("com.mysql.cj.jdbc.Driver");
    return DriverManager.getConnection(URL, USER, PASS);
  }
}