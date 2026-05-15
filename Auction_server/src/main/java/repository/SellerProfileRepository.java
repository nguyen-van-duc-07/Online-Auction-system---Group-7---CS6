package repository;

import com.auction.shared.enums.SellerRegisterStatus;
import com.auction.shared.model.user.SellerProfile;
import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SellerProfileRepository {
  public boolean createSellerProfile(SellerProfile sellerProfile){
    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "INSERT INTO seller_profiles (id, user_id, brand_name, cccd, location, bank_account, bank_name, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, sellerProfile.getId());
      ps.setString(2, sellerProfile.getUserId());
      ps.setString(3, sellerProfile.getBrandName());
      ps.setString(4, sellerProfile.getCitizenIdentityCard());
      ps.setString(5, sellerProfile.getLocation());
      ps.setString(6, sellerProfile.getBankAccount());
      ps.setString(7, sellerProfile.getBankName());
      ps.setString(8, SellerRegisterStatus.UNREGISTERED.toString());
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

  /**
   * Lấy trạng thái hồ sơ người bán dựa trên ID của User.
   * @param userId ID của người dùng
   * @return Chuỗi trạng thái ("PENDING", "APPROVED", "REJECTED") hoặc null nếu chưa có hồ sơ
   */
  public String getSellerProfileStatus(String userId) {
    String sql = "SELECT status FROM seller_profiles WHERE user_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, userId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getString("status"); // Trả về trạng thái của hồ sơ
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null; // Trả về null nếu User này chưa từng đăng ký làm người bán
  }

  public String getUserIdByProfileId(String profileId) {
    String sql = "SELECT user_id FROM seller_profiles WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, profileId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString("user_id");
      }
      return null;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }
}

