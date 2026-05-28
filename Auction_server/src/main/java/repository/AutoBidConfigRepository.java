package repository;

import com.auction.shared.model.auction.AutoBidConfig;
import config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoBidConfigRepository {
  private static final Logger log = LoggerFactory.getLogger(AutoBidConfigRepository.class);

  public boolean save(AutoBidConfig config) {
    // Nếu đã có config cho user+auction này thì UPDATE, không thì INSERT
    String sql = "INSERT INTO auto_bid_configs (id, user_id, auction_id, max_price, step_amount, is_active) "
        + "VALUES (?, ?, ?, ?, ?, ?) "
        + "ON DUPLICATE KEY UPDATE max_price = ?, step_amount = ?, is_active = TRUE, create_at = NOW()";

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
      log.error("Lỗi cơ sở dữ liệu khi lưu cấu hình tự động đấu giá của user: {}", config.getUserId(), e);
      return false;
    }
  }

  public AutoBidConfig findActiveByUserIdAndAuctionId(String userId, String auctionId) {
    String sql = "SELECT * FROM auto_bid_configs "
        + "WHERE user_id = ? AND auction_id = ? AND is_active = TRUE";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, userId);
      ps.setString(2, auctionId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapRow(rs);
        }
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi tìm kiếm cấu hình tự động đấu giá của user: {} và phiên: {}", userId, auctionId, e);
    }
    return null;
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

  public List<AutoBidConfig> findActiveBotsOrderedByMaxPrice(Connection conn, String auctionId) throws SQLException {
    List<AutoBidConfig> activeBots = new ArrayList<>();
    String sql = "SELECT * FROM auto_bid_configs WHERE auction_id = ? AND is_active = 1 "
        + "ORDER BY max_price DESC, create_at ASC";

    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, auctionId);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          AutoBidConfig bot = new AutoBidConfig();
          bot.setId(rs.getString("id"));
          bot.setUserId(rs.getString("user_id"));
          bot.setAuctionId(rs.getString("auction_id"));
          bot.setMaxPrice(rs.getBigDecimal("max_price"));
          bot.setStepAmount(rs.getBigDecimal("step_amount"));
          bot.setActive(rs.getBoolean("is_active"));
          activeBots.add(bot);
        }
      }
    }
    return activeBots;
  }
  // Tắt Bot và dùng chung Connection
  public void deactivate(Connection conn, String userId, String auctionId) throws SQLException {
    String sql = "UPDATE auto_bid_configs SET is_active = FALSE WHERE user_id = ? AND auction_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, userId);
      ps.setString(2, auctionId);
      ps.executeUpdate();
    }
  }

  public boolean deactivate(String userId, String auctionId) {
    String sql = "UPDATE auto_bid_configs SET is_active = FALSE WHERE user_id = ? AND auction_id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, userId);
      ps.setString(2, auctionId);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi tắt tự động đấu giá cho user: {} và phiên: {}", userId, auctionId, e);
      return false;
    }
  }
}
