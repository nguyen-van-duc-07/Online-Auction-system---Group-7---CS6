package repository;

import com.auction.shared.enums.SellerRegisterStatus;
import com.auction.shared.model.user.InfoDTO;
import com.auction.shared.model.user.SellerProfile;
import com.auction.shared.model.user.ShopInfoDTO;
import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SellerProfileRepository {
  private static final Logger log = LoggerFactory.getLogger(SellerProfileRepository.class);

  private SellerProfileRepository() {}

  private static class Holder {
    private static final SellerProfileRepository INSTANCE = new SellerProfileRepository();
  }

  public static SellerProfileRepository getInstance() {
    return Holder.INSTANCE;
  }
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
      log.error("Lỗi cơ sở dữ liệu khi tạo hồ sơ người bán cho user: {}", sellerProfile.getUserId(), e);
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
      log.error("Lỗi cơ sở dữ liệu khi tìm kiếm hồ sơ người bán cho user ID: {}", userId, e);
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
      log.error("Lỗi cơ sở dữ liệu khi lấy trạng thái hồ sơ người bán cho user ID: {}", userId, e);
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
      log.error("Lỗi cơ sở dữ liệu khi lấy userId bằng profileId: {}", profileId, e);
      return null;
    }
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
      log.error("Lỗi cơ sở dữ liệu khi lấy toàn bộ danh sách hồ sơ người bán", e);
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
      log.error("Lỗi cơ sở dữ liệu khi lấy userId bằng sellerId: {}", sellerId, e);
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
      log.error("Lỗi cơ sở dữ liệu khi cập nhật trạng thái hồ sơ người bán cho user: {}", userId, e);
      return false;
    }
  }
  public boolean haveSellerProfile(String userId) {
    String sql = "SELECT 1 FROM seller_profiles WHERE user_id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, userId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi kiểm tra sự tồn tại hồ sơ người bán cho user ID: {}", userId, e);
      return false;
    }
  }
  public ShopInfoDTO getShopInfo(String sellerId) {
    String query = "SELECT brand_name, location "
        + "FROM seller_profiles "
        + "WHERE user_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

      pstmt.setString(1, sellerId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          ShopInfoDTO shopInfo = new ShopInfoDTO();
          shopInfo.setBrandName(rs.getString("brand_name"));
          shopInfo.setLocation(rs.getString("location"));
          return shopInfo;
        }
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy thông tin của cửa hàng cho seller ID: {}", sellerId, e);
    }
    return null;
  }
}

