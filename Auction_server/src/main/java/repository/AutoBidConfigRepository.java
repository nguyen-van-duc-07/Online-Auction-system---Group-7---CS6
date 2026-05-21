package repository;

import com.auction.shared.model.auction.AutoBidConfig;
import config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AutoBidConfigRepository {

  public boolean save(AutoBidConfig config) {
    // Nếu đã có config cho user+auction này thì UPDATE, không thì INSERT
    String sql = "INSERT INTO auto_bid_configs (id, user_id, auction_id, max_price, step_amount, is_active) "
        + "VALUES (?, ?, ?, ?, ?, ?) "
        + "ON DUPLICATE KEY UPDATE max_price = ?, step_amount = ?, is_active = TRUE";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, config.getId());
      ps.setString(2, config.getUserId());
      ps.setString(3, config.getAuctionId());
      ps.setBigDecimal(4, config.getMaxPrice());
      ps.setBigDecimal(5, config.getStepAmount());
      ps.setBoolean(6, config.isActive());
      ps.setBigDecimal(7, config.getMaxPrice());
      ps.setBigDecimal(8, config.getStepAmount());

      return ps.executeUpdate() > 0;

    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  // Lấy tất cả config đang active trong 1 phiên, trừ người vừa bid
  public List<AutoBidConfig> findActiveByAuctionId(String auctionId, String excludeUserId) {
    List<AutoBidConfig> configs = new ArrayList<>();
    String sql = "SELECT * FROM auto_bid_configs "
        + "WHERE auction_id = ? AND user_id != ? AND is_active = TRUE "
        + "ORDER BY max_price DESC"; // ưu tiên người có max cao hơn

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, auctionId);
      ps.setString(2, excludeUserId);
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        configs.add(mapRow(rs));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return configs;
  }

  // Tắt autobid khi user thắng hoặc auction kết thúc
  public void deactivate(String userId, String auctionId) {
    String sql = "UPDATE auto_bid_configs SET is_active = FALSE "
        + "WHERE user_id = ? AND auction_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, userId);
      ps.setString(2, auctionId);
      ps.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private AutoBidConfig mapRow(ResultSet rs) throws SQLException {
    AutoBidConfig config = new AutoBidConfig();
    config.setId(rs.getString("id"));
    config.setUserId(rs.getString("user_id"));
    config.setAuctionId(rs.getString("auction_id"));
    config.setMaxPrice(rs.getBigDecimal("max_price"));
    config.setStepAmount(rs.getBigDecimal("step_amount"));
    config.setActive(rs.getBoolean("is_active"));
    return config;
  }

  public List<AutoBidConfig> findActiveBotsOrderedByMaxPrice(String auctionId) {
    List<AutoBidConfig> activeBots = new ArrayList<>();

    String sql = "SELECT * FROM auto_bid_configs WHERE auction_id = ? AND is_active = 1 ORDER BY max_price DESC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, auctionId);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          AutoBidConfig bot = new AutoBidConfig();

          // LƯU Ý: id của bạn là chuỗi UUID (như trong ảnh), nên dùng getString thay vì getInt
          bot.setId(rs.getString("id"));
          bot.setUserId(rs.getString("user_id"));
          bot.setAuctionId(rs.getString("auction_id"));
          bot.setMaxPrice(rs.getBigDecimal("max_price"));

          // Map đúng tên cột step_amount trong DB vào biến của Object
          bot.setStepAmount(rs.getBigDecimal("step_amount"));

          bot.setActive(rs.getBoolean("is_active"));

          activeBots.add(bot);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Lỗi khi truy vấn danh sách Bot: " + e.getMessage());
    }
    return activeBots;
  }
}