package repository;

import com.auction.shared.model.user.SellerProfile;
import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SellerProfileRepository {
  public boolean createSellerProfile(SellerProfile sellerProfile){
    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "INSERT INTO seller_profiles (id, user_id) VALUES (?, ?)";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, sellerProfile.getId());
      ps.setString(2, sellerProfile.getManagerId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  // Hàm kiểm tra và lấy ID hồ sơ người bán dựa vào ID của User
  public String findProfileIdByUserId(String userId) {
    String sql = "SELECT id FROM seller_profiles WHERE user_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, userId);

      try (java.sql.ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getString("id"); // Trả về ID của hồ sơ người bán nếu tìm thấy
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null; // Trả về null nếu User này chưa đăng ký làm người bán
  }
}

