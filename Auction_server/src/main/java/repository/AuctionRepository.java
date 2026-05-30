package repository;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.enums.ItemType;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.item.ItemDTO;
import com.auction.shared.response.AuctionResponseDTO;
import config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionRepository {
  private static final Logger log = LoggerFactory.getLogger(AuctionRepository.class);

  public List<AuctionDTO> findAuctionsByStatusForBidder(AuctionStatus status) {
    List<AuctionDTO> auctions = new ArrayList<>();
    String sql = "SELECT id, start_time, end_time, status, current_price, item_name, item_type "
        + "FROM auctions "
        + "WHERE status = ? "
        + "ORDER BY start_time ASC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);) {
      ps.setString(1, status.name());
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        AuctionDTO auction = mapResultSetToAuctionDTO(rs);
        auctions.add(auction);
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi tìm kiếm đấu giá đang chờ", e);
    }
    return auctions;
  }

  // Lấy tất cả các phiên đấu giá đang mở hoặc sắp diễn ra
  public List<AuctionDTO> findActiveAndWaitingAuctions() {
    List<AuctionDTO> auctions = new ArrayList<>();
    // Sử dụng IN để lấy cả hai trạng thái WAITING và ACTIVE, kết hợp sắp xếp theo thứ tự ACTIVE -> WAITING
    String sql = "SELECT id, start_time, end_time, status, current_price, item_name, item_type "
        + "FROM auctions "
        + "WHERE status IN ('WAITING', 'ACTIVE') "
        + "ORDER BY CASE status "
        + "  WHEN 'ACTIVE' THEN 1 "
        + "  WHEN 'WAITING' THEN 2 "
        + "  ELSE 3 "
        + "END, start_time ASC";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        AuctionDTO auction = mapResultSetToAuctionDTO(rs);
        auctions.add(auction);
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi tìm kiếm đấu giá đang hoạt động hoặc đang chờ", e);
    }
    return auctions;
  }

  public List<AuctionDTO> findAuctionsByUserId(String userId) {
    List<AuctionDTO> auctions = new ArrayList<>();
    String sql = "SELECT id, start_time, end_time, status, current_price, item_name, item_type "
        + "FROM auctions "
        + "WHERE user_id = ? "
        + "ORDER BY CASE status "
        + "  WHEN 'ACTIVE' THEN 1 "
        + "  WHEN 'WAITING' THEN 2 "
        + "  WHEN 'CLOSED' THEN 3 "
        + "  ELSE 4 "
        + "END, start_time ASC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      // Gán giá trị userId vào dấu ? trong câu lệnh SQL
      ps.setString(1, userId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          AuctionDTO auction = mapResultSetToAuctionDTO(rs);
          auctions.add(auction);
        }
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi tìm kiếm đấu giá của người bán ID: {}", userId, e);
    }
    return auctions;
  }

  // Lấy tất cả các phiên đấu giá đang mở thuộc về một seller
  public List<AuctionDTO> findAuctionsByUserIdAndStatus(String userId, AuctionStatus status) {
    List<AuctionDTO> auctions = new ArrayList<>();
    String sql = "SELECT id, start_time, end_time, status, current_price, item_name, item_type "
        + "FROM auctions "
        + "WHERE status = ? AND user_id = ?"
        + "ORDER BY start_time ASC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, status.name());
      ps.setString(2, userId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          AuctionDTO auction = mapResultSetToAuctionDTO(rs);
          auctions.add(auction);
        }
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi tìm kiếm đấu giá đang hoạt động của người bán ID: {}", userId, e);
    }
    return auctions;
  }


  public boolean cancelActiveAndWaitingAuctionsByUserId(String userId) {
    String selectSql = "SELECT id, highest_bidder_id, current_price FROM auctions WHERE user_id = ? AND status = 'ACTIVE'";
    String updateSql = "UPDATE auctions SET status = ? WHERE user_id = ? AND status IN (?, ?)";
    String deactivateBotsSql = "UPDATE auto_bid_configs SET is_active = FALSE WHERE auction_id = ?";

    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        // 1. Lấy tất cả phiên ACTIVE của seller này và giải phóng cọc cho người chơi dẫn đầu
        try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
          psSelect.setString(1, userId);
          try (ResultSet rs = psSelect.executeQuery()) {
            service.WalletService walletService = new service.WalletService();
            while (rs.next()) {
              String auctionId = rs.getString("id");
              String highestBidderId = rs.getString("highest_bidder_id");
              java.math.BigDecimal currentPrice = rs.getBigDecimal("current_price");

              if (highestBidderId != null && !highestBidderId.isEmpty() && currentPrice != null) {
                java.math.BigDecimal releaseAmount = currentPrice.multiply(new java.math.BigDecimal("0.1"));
                walletService.releaseFrozen(conn, highestBidderId, releaseAmount, auctionId);
                log.info("[SELLER REJECT - RELEASE] Hoàn trả cọc {} cho user {} từ phiên {}", releaseAmount, highestBidderId, auctionId);
              }

              // 2. Vô hiệu hóa bot của phiên này
              try (PreparedStatement psBot = conn.prepareStatement(deactivateBotsSql)) {
                psBot.setString(1, auctionId);
                psBot.executeUpdate();
              }
            }
          }
        }

        // 3. Cập nhật trạng thái tất cả phiên sang CANCELED
        try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
          psUpdate.setString(1, AuctionStatus.CANCELED.name());
          psUpdate.setString(2, userId);
          psUpdate.setString(3, AuctionStatus.ACTIVE.name());
          psUpdate.setString(4, AuctionStatus.WAITING.name());
          psUpdate.executeUpdate();
        }

        conn.commit();
        return true;
      } catch (Exception e) {
        conn.rollback();
        log.error("Lỗi khi hủy các đấu giá của người bán ID: {}", userId, e);
        return false;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.error("Lỗi kết nối cơ sở dữ liệu khi hủy các đấu giá của người bán ID: {}", userId, e);
      return false;
    }
  }

  public boolean restoreCanceledAuctionsByUserId(String userId, LocalDateTime now) {
    String selectSql = "SELECT id FROM auctions WHERE user_id = ? AND status = ?";

    String updateSql = "UPDATE auctions SET "
        + "current_price = start_price, "
        + "highest_bidder_id = NULL, "
        + "status = CASE "
        + "WHEN end_time <= ? THEN ? "
        + "WHEN start_time <= ? AND end_time > ? THEN ? "
        + "WHEN start_time > ? THEN ? "
        + "ELSE status END "
        + "WHERE user_id = ? AND status = ?";

    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        // 1. Lấy danh sách ID các phiên bị hủy
        List<String> canceledIds = new ArrayList<>();
        try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
          psSelect.setString(1, userId);
          psSelect.setString(2, AuctionStatus.CANCELED.name());
          try (ResultSet rs = psSelect.executeQuery()) {
            while (rs.next()) {
              canceledIds.add(rs.getString("id"));
            }
          }
        }

        // 2. Xóa toàn bộ lịch sử đặt giá cũ của các phiên bị hủy (nếu có)
        if (!canceledIds.isEmpty()) {
          BidTransactionRepository bidRepo = new BidTransactionRepository();
          bidRepo.deleteByAuctionIds(conn, canceledIds);
        }

        // 3. Reset giá về khởi điểm, xóa người dẫn đầu và khôi phục trạng thái
        try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
          Timestamp currentTimestamp = Timestamp.valueOf(now);
          psUpdate.setTimestamp(1, currentTimestamp);
          psUpdate.setString(2, AuctionStatus.CLOSED.name());
          psUpdate.setTimestamp(3, currentTimestamp);
          psUpdate.setTimestamp(4, currentTimestamp);
          psUpdate.setString(5, AuctionStatus.ACTIVE.name());
          psUpdate.setTimestamp(6, currentTimestamp);
          psUpdate.setString(7, AuctionStatus.WAITING.name());
          psUpdate.setString(8, userId);
          psUpdate.setString(9, AuctionStatus.CANCELED.name());
          psUpdate.executeUpdate();
        }

        conn.commit();
        log.info("[RESTORE] Đã khôi phục và reset thành công {} phiên đấu giá của seller {}", canceledIds.size(), userId);
        return true;
      } catch (Exception e) {
        conn.rollback();
        log.error("Lỗi khi khôi phục các đấu giá của người bán ID: {}", userId, e);
        return false;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.error("Lỗi kết nối cơ sở dữ liệu khi khôi phục các đấu giá của người bán ID: {}", userId, e);
      return false;
    }
  }

  public boolean restoreSingleCanceledAuction(String auctionId, LocalDateTime now) {
    String selectSql = "SELECT id FROM auctions WHERE id = ? AND status = ?";

    String updateSql = "UPDATE auctions SET "
        + "current_price = start_price, "
        + "highest_bidder_id = NULL, "
        + "status = CASE "
        + "WHEN end_time <= ? THEN ? "
        + "WHEN start_time <= ? AND end_time > ? THEN ? "
        + "WHEN start_time > ? THEN ? "
        + "ELSE status END "
        + "WHERE id = ? AND status = ?";

    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        boolean exists = false;
        try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
          psSelect.setString(1, auctionId);
          psSelect.setString(2, AuctionStatus.CANCELED.name());
          try (ResultSet rs = psSelect.executeQuery()) {
            if (rs.next()) {
              exists = true;
            }
          }
        }

        if (!exists) {
          conn.rollback();
          return false;
        }

        BidTransactionRepository bidRepo = new BidTransactionRepository();
        List<String> auctionIds = new ArrayList<>();
        auctionIds.add(auctionId);
        bidRepo.deleteByAuctionIds(conn, auctionIds);

        try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
          Timestamp currentTimestamp = Timestamp.valueOf(now);
          psUpdate.setTimestamp(1, currentTimestamp);
          psUpdate.setString(2, AuctionStatus.CLOSED.name());
          psUpdate.setTimestamp(3, currentTimestamp);
          psUpdate.setTimestamp(4, currentTimestamp);
          psUpdate.setString(5, AuctionStatus.ACTIVE.name());
          psUpdate.setTimestamp(6, currentTimestamp);
          psUpdate.setString(7, AuctionStatus.WAITING.name());
          psUpdate.setString(8, auctionId);
          psUpdate.setString(9, AuctionStatus.CANCELED.name());
          psUpdate.executeUpdate();
        }

        conn.commit();
        log.info("[RESTORE-SINGLE] Đã khôi phục và reset thành công phiên đấu giá {}", auctionId);
        return true;
      } catch (Exception e) {
        conn.rollback();
        log.error("Lỗi khi khôi phục phiên đấu giá đơn lẻ ID: {}", auctionId, e);
        return false;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.error("Lỗi kết nối cơ sở dữ liệu khi khôi phục phiên đấu giá đơn lẻ ID: {}", auctionId, e);
      return false;
    }
  }



  public void updatePrice(Connection conn, String auctionId, String bidderId, java.math.BigDecimal newPrice) throws SQLException {
    String sql = "UPDATE auctions SET current_price = ?, highest_bidder_id = ? WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setBigDecimal(1, newPrice);
      ps.setString(2, bidderId);
      ps.setString(3, auctionId);
      ps.executeUpdate();
    }
  }

  // Hàm phụ trợ mapping dữ liệu và trả về AuctionDTO
  private AuctionDTO mapResultSetToAuctionDTO(ResultSet rs) throws SQLException {
    AuctionDTO auctionDTO = new AuctionDTO();
    auctionDTO.setAuctionId(rs.getString("id"));
    auctionDTO.setItemName(rs.getString("item_name"));
    auctionDTO.setCurrentPrice(rs.getBigDecimal("current_price"));
    auctionDTO.setStatus(AuctionStatus.valueOf(rs.getString("status")));
    auctionDTO.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
    auctionDTO.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());

    // Mapping loại sản phẩm
    String itemTypeStr = rs.getString("item_type");
    if (itemTypeStr != null) {
      try {
        auctionDTO.setItemType(ItemType.valueOf(itemTypeStr.toUpperCase()));
      } catch (IllegalArgumentException e) {
        auctionDTO.setItemType(ItemType.OTHER);
      }
    }

    return auctionDTO;
  }

  // Hàm phụ trợ để mapping dữ liệu (tránh viết lặp code)
  private AuctionResponseDTO mapResultSetToAuctionResponseDTO(ResultSet rs) throws SQLException {
    String typeStr = rs.getString("item_type");
    ItemType itemType;
    try {
      itemType = (typeStr != null) ? ItemType.valueOf(typeStr.toUpperCase()) : ItemType.OTHER;
    } catch (IllegalArgumentException e) {
      // Mặc định là OTHER nếu dưới Database lỡ nhập sai chữ
      itemType = ItemType.OTHER;
    }
    ItemDTO item = new ItemDTO();
    item.setId(rs.getString("id"));
    item.setName(rs.getString("item_name"));
    item.setType(itemType);
    item.setDescription(rs.getString("item_description"));

    String attributesJson = rs.getString("attributes");
    if (attributesJson != null && !attributesJson.isEmpty()) {
      java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.Map<String, String>>() {
      }.getType();
      item.setAdditionalAttributes(new com.google.gson.Gson().fromJson(attributesJson, type));
    }

    AuctionResponseDTO auction = new AuctionResponseDTO();
    auction.setId(rs.getString("id"));
    auction.setItem(item);
    auction.setStartPrice(rs.getBigDecimal("start_price"));
    auction.setCurrentHighestPrice(rs.getBigDecimal("current_price"));
    auction.setMinStepPrice(rs.getBigDecimal("min_step_price"));
    auction.setStatus(AuctionStatus.valueOf(rs.getString("status")));
    auction.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
    auction.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());

    UserRepository userRepo = new UserRepository();
    String highestBidderId = rs.getString("highest_bidder_id");
    String highestBidderName = userRepo.getAccountNameByUserId(rs.getString("highest_bidder_id"));

    String userId = rs.getString("user_id");
    auction.setUserId(userId);

    auction.setHighestBidderId(highestBidderId);
    auction.setHighestBidderName(highestBidderName);

    // Đọc đường dẫn ảnh sản phẩm
    auction.setImagePath(rs.getString("image_path"));

    return auction;
  }

  public boolean saveAuction(Auction auction, String imagePath) {
    String sql = "INSERT INTO auctions (id, user_id, start_price, min_step_price, current_price, highest_bidder_id, start_time, end_time, status, image_path, item_name, item_type, item_description, attributes) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, auction.getId());
      ps.setString(2, auction.getUserId());
      ps.setBigDecimal(3, auction.getStartPrice());
      ps.setBigDecimal(4, auction.getMinStepPrice());
      ps.setBigDecimal(5, auction.getCurrentHighestPrice());
      ps.setString(6, auction.getHighestBidderId());
      ps.setTimestamp(7, java.sql.Timestamp.valueOf(auction.getStartTime()));
      ps.setTimestamp(8, java.sql.Timestamp.valueOf(auction.getEndTime()));
      ps.setString(9, auction.getStatus().name());
      ps.setString(10, imagePath);
      ps.setString(11, auction.getItem().getName());
      ps.setString(12, auction.getItem().getType().name());
      ps.setString(13, auction.getItem().getDescription());

      String attributesJson = null;
      if (auction.getItem().getAdditionalAttributes() != null) {
        attributesJson = new com.google.gson.Gson().toJson(auction.getItem().getAdditionalAttributes());
      }
      ps.setString(14, attributesJson);

      return ps.executeUpdate() > 0;

    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lưu thông tin đấu giá ID: {}", auction.getId(), e);
      return false;
    }
  }

  /**
   * Đóng phiên chỉ khi vẫn ACTIVE và end_time đã qua — tránh đóng nhầm sau anti-sniping.
   *
   * @return true nếu vừa chuyển sang CLOSED
   */
  public boolean tryCloseExpiredAuction(String auctionId, LocalDateTime now) {
    String sql = "UPDATE auctions SET status = ? WHERE id = ? AND status = ? AND end_time <= ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, AuctionStatus.CLOSED.name());
      ps.setString(2, auctionId);
      ps.setString(3, AuctionStatus.ACTIVE.name());
      ps.setTimestamp(4, Timestamp.valueOf(now));
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi thử đóng đấu giá ID: {}", auctionId, e);
      return false;
    }
  }
  public boolean tryActivateAuction(String auctionId, LocalDateTime now) {
    String sql = "UPDATE auctions SET status = ? WHERE id = ? AND status = ? AND start_time <= ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, AuctionStatus.ACTIVE.name());
      ps.setString(2, auctionId);
      ps.setString(3, AuctionStatus.WAITING.name());
      ps.setTimestamp(4, Timestamp.valueOf(now));
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi thử đóng đấu giá ID: {}", auctionId, e);
      return false;
    }
  }

  public Map<String, AuctionResponseDTO> findAuctionsToActivate(LocalDateTime now) {

    Map<String, AuctionResponseDTO> result = new LinkedHashMap<>();

    String sql =
        "SELECT * "
            + "FROM auctions "
            + "WHERE status = 'WAITING' AND start_time <= ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setTimestamp(1, Timestamp.valueOf(now));
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        AuctionResponseDTO auction = mapResultSetToAuctionResponseDTO(rs);
        result.put(auction.getId(), auction);
      }
    } catch (Exception e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy chi tiết đấu giá cần đóng", e);
    }

    return result;
  }

  public void activateAuctions(List<String> ids) {

    String sql =
        "UPDATE auctions SET status='ACTIVE' WHERE id=?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      for (String id : ids) {
        ps.setString(1, id);
        ps.executeUpdate();
      }

    } catch (Exception e) {
      log.error("Lỗi cơ sở dữ liệu khi kích hoạt đấu giá: {}", ids, e);
    }
  }

  public AuctionResponseDTO findAuctionById(String auctionId) {
    // Cập nhật câu SQL tương tự như trên
    String sql = "SELECT a.*, u.account_name AS highest_bidder_name "
        + "FROM auctions a "
        + "LEFT JOIN users u ON a.highest_bidder_id = u.id "
        + "WHERE a.id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, auctionId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return mapResultSetToAuctionResponseDTO(rs);
      }
      return null;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public AuctionResponseDTO findAuctionResponseDTOById(String auctionId) {
    // Cập nhật câu SQL tương tự như trên
    String sql = "SELECT a.*, u.account_name AS highest_bidder_name "
        + "FROM auctions a "
        + "LEFT JOIN users u ON a.highest_bidder_id = u.id "
        + "WHERE a.id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, auctionId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return mapResultSetToAuctionResponseDTO(rs);
      }
      return null;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  // Trả về Map<auctionId, Auction> thay vì chỉ List<String>
  public Map<String, AuctionResponseDTO> findAuctionsToCloseWithDetails(LocalDateTime now) {
    Map<String, AuctionResponseDTO> result = new LinkedHashMap<>();

    String sql =
        "SELECT a.* "
            + "FROM auctions a "
            + "WHERE a.status = 'ACTIVE' AND a.end_time <= ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setTimestamp(1, Timestamp.valueOf(now));
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        AuctionResponseDTO auction = mapResultSetToAuctionResponseDTO(rs);
        result.put(auction.getId(), auction);
      }
    } catch (Exception e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy chi tiết đấu giá cần đóng", e);
    }

    return result;
  }

  public String getUserIdByAuctionId(String auctionId) {
    String sql = "SELECT user_id FROM auctions WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, auctionId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString("user_id");
      }
      return null;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy userId cho đấu giá ID: {}", auctionId, e);
      return null;
    }
  }

  public void updateEndTime(Connection conn, String auctionId, LocalDateTime newEndTime) throws SQLException {
    String sql = "UPDATE auctions SET end_time = ? WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setTimestamp(1, Timestamp.valueOf(newEndTime));
      ps.setString(2, auctionId);
      ps.executeUpdate();
    }
  }

  public AuctionResponseDTO findAuctionForUpdate(Connection conn, String auctionId) throws SQLException {
    String sql = "SELECT a.*, u.account_name AS highest_bidder_name "
        + "FROM auctions a "
        + "LEFT JOIN users u ON a.highest_bidder_id = u.id "
        + "WHERE a.id = ? FOR UPDATE"; // Khóa dòng này lại cho đến khi commit

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, auctionId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToAuctionResponseDTO(rs);
        }
      }
    }
    return null;
  }

  public boolean updateAuctionStatusAndStartTime(String auctionId, String status, LocalDateTime startTime) {
    String sql = "UPDATE auctions SET status = ?, start_time = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, status);
      ps.setTimestamp(2, Timestamp.valueOf(startTime));
      ps.setString(3, auctionId);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi cập nhật trạng thái và start_time cho đấu giá ID: {}", auctionId, e);
      return false;
    }
  }

  public boolean updateAuctionEndTime(String auctionId, LocalDateTime endTime) {
    String sql = "UPDATE auctions SET end_time = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setTimestamp(1, Timestamp.valueOf(endTime));
      ps.setString(2, auctionId);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi cập nhật end_time cho đấu giá ID: {}", auctionId, e);
      return false;
    }
  }

  public boolean cancelAuctionAndReleaseDeposit(String auctionId) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        AuctionResponseDTO auction = findAuctionForUpdate(conn, auctionId);
        if (auction == null || auction.getStatus() == AuctionStatus.CANCELED || auction.getStatus() == AuctionStatus.CLOSED) {
          conn.rollback();
          return false;
        }

        String sql = "UPDATE auctions SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
          ps.setString(1, AuctionStatus.CANCELED.name());
          ps.setString(2, auctionId);
          ps.executeUpdate();
        }

        if (auction.getHighestBidderId() != null && !auction.getHighestBidderId().isEmpty() && auction.getCurrentHighestPrice() != null) {
          java.math.BigDecimal releaseAmount = auction.getCurrentHighestPrice().multiply(new java.math.BigDecimal("0.1"));
          new service.WalletService().releaseFrozen(conn, auction.getHighestBidderId(), releaseAmount, auctionId);
          log.info("[CANCEL - RELEASE] Hoàn trả cọc {} cho user {} khi hủy đấu giá {}", releaseAmount, auction.getHighestBidderId(), auctionId);
        }

        String botSql = "UPDATE auto_bid_configs SET is_active = FALSE WHERE auction_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(botSql)) {
          ps.setString(1, auctionId);
          ps.executeUpdate();
        }

        conn.commit();
        return true;
      } catch (Exception e) {
        conn.rollback();
        log.error("Lỗi khi hủy phiên đấu giá và giải phóng cọc ID: {}", auctionId, e);
        return false;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.error("Lỗi kết nối cơ sở dữ liệu khi hủy đấu giá ID: {}", auctionId, e);
      return false;
    }
  }
}

