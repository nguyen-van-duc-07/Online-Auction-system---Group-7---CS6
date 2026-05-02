package repository;

import com.auction.shared.model.user.User;
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

  public boolean isAccountExist(String accountName) {
    // Dùng SELECT 1 cho tốc độ truy vấn tối đa
    String sql = "SELECT 1 FROM users WHERE account_name = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, accountName);

      try (ResultSet rs = ps.executeQuery()) {
        // rs.next() sẽ trả về true nếu Database tìm thấy ít nhất 1 dòng khớp tên
        return rs.next();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false; // Mặc định nếu có lỗi hoặc không tìm thấy thì coi như chưa tồn tại
  }

  public boolean createUser(String id, String accountName, String hashedPassword) {
    try (Connection conn = DBConnection.getConnection()) {
      // Lệnh SQL để chèn thêm 1 dòng mới vào bảng users
      String sql = "INSERT INTO users (id, account_name, password) VALUES (?, ?, ?)";
      PreparedStatement ps = conn.prepareStatement(sql);

      // Lắp thông tin vào các dấu ?
      ps.setString(1, id);
      ps.setString(2, accountName);
      ps.setString(3, hashedPassword);

      // Thực thi lệnh. executeUpdate() trả về số dòng bị ảnh hưởng trong DB
      int rowsAffected = ps.executeUpdate();
      return rowsAffected > 0; // Trả về true nếu chèn thành công ít nhất 1 dòng
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }
}