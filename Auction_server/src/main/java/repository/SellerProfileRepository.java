package repository;

import com.auction.shared.enums.SellerRegisterStatus;
import com.auction.shared.model.user.SellerProfile;
import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

  /**
   * Truy vấn toàn bộ danh sách hồ sơ người bán (Seller Profile) có trong hệ thống.
   * Lấy đầy đủ các thuộc tính bao gồm cả thời điểm tạo hồ sơ.
   *
   * @return Danh sách các đối tượng {@link SellerProfile}.
   */
  public List<SellerProfile> getAllSellerProfiles() {
    List<SellerProfile> sellerList = new ArrayList<>();
    String sql = "SELECT * FROM seller_profiles";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        SellerProfile profile = new SellerProfile();

        profile.setUserId(rs.getString("user_id"));
        profile.setBrandName(rs.getString("brand_name"));
        profile.setCitizenIdentityCard(rs.getString("cccd"));
        profile.setLocation(rs.getString("location"));
        profile.setBankAccount(rs.getString("bank_account"));
        profile.setBankName(rs.getString("bank_name"));
        profile.setStatus(rs.getString("status"));

        // Lấy dữ liệu thời gian dạng Timestamp từ Database
        java.sql.Timestamp timestamp = rs.getTimestamp("created_at");

        // Kiểm tra null để tránh lỗi NullPointerException
        if (timestamp != null) {
          profile.setCreatedAt(timestamp.toLocalDateTime()); // Chuyển sang LocalDateTime
        }

        sellerList.add(profile);
      }
    } catch (SQLException e) {
      System.err.println("Lỗi khi lấy danh sách Seller: " + e.getMessage());
      e.printStackTrace();
    }

    return sellerList;
  }

  public String getUserIdBySellerId(String sellerId) {
    // Câu lệnh SQL tìm user_id dựa vào id của bảng seller_profiles
    String sql = "SELECT user_id FROM seller_profiles WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      // Gán tham số sellerId vào dấu ?
      ps.setString(1, sellerId);

      try (ResultSet rs = ps.executeQuery()) {
        // Nếu tìm thấy kết quả, trả về cột user_id
        if (rs.next()) {
          return rs.getString("user_id");
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // Trả về null nếu không tìm thấy hoặc có lỗi xảy ra
    return null;
  }

  public boolean updateStatus(String userId, SellerRegisterStatus status) {
    String sql = "UPDATE seller_profiles SET status = ? WHERE user_id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, status.toString());
      ps.setString(2, userId);
      return ps.executeUpdate() == 1;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
}

