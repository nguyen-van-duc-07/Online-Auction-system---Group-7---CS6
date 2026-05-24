package repository;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.enums.ItemType;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.item.Item;
import com.auction.shared.model.item.ItemDTO;
import com.auction.shared.response.AuctionResponseDTO;
import config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AuctionRepository {
  // Lấy tất cả các phiên đấu giá đang mở
  public List<AuctionDTO> findActiveAuctions() {
    List<AuctionDTO> auctions = new ArrayList<>();
    // Cập nhật câu SQL: Thêm LEFT JOIN với bảng users và lấy cột real_name
    String sql = "SELECT a.id, a.start_time, a.end_time, a.status, a.current_price, i.name AS item_name "
            + "FROM auctions a "
            + "JOIN items i ON a.item_id = i.id "
            + "WHERE a.status = 'ACTIVE'"
            + "ORDER BY a.start_time ASC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        AuctionDTO auction = mapResultSetToAuctionDTO(rs);
        auctions.add(auction);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return auctions;
  }

  // Lấy tất cả các phiên đấu giá đang chờ bắt đầu
  public List<AuctionDTO> findWaitingAuctions() {
    List<AuctionDTO> auctions = new ArrayList<>();
    String sql = "SELECT a.id, a.start_time, a.end_time, a.status, a.current_price, i.name AS item_name "
            + "FROM auctions a "
            + "JOIN items i ON a.item_id = i.id "
            + "WHERE a.status = 'WAITING' "
            + "ORDER BY a.start_time ASC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        AuctionDTO auction = mapResultSetToAuctionDTO(rs);
        auctions.add(auction);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return auctions;
  }

  // Lấy tất cả các phiên đấu giá đã kết thúc
  public List<AuctionDTO> findClosedAuctions() {
    List<AuctionDTO> auctions = new ArrayList<>();
    String sql = "SELECT a.id, a.start_time, a.end_time, a.status, a.current_price, i.name AS item_name "
            + "FROM auctions a "
            + "JOIN items i ON a.item_id = i.id "
            + "WHERE a.status = 'CLOSED' "
            + "ORDER BY a.end_time DESC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        AuctionDTO auction = mapResultSetToAuctionDTO(rs);
        auctions.add(auction);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return auctions;
  }

  // Lấy tất cả các phiên đấu giá đang mở hoặc sắp diễn ra
  public List<AuctionDTO> findActiveAndWaitingAuctions() {
    List<AuctionDTO> auctions = new ArrayList<>();
    // Sử dụng IN để lấy cả hai trạng thái WAITING và ACTIVE, kết hợp sắp xếp theo thứ tự ACTIVE -> WAITING
    String sql = "SELECT a.id, a.start_time, a.end_time, a.status, a.current_price, i.name AS item_name "
            + "FROM auctions a "
            + "JOIN items i ON a.item_id = i.id "
            + "WHERE a.status IN ('WAITING', 'ACTIVE') "
            + "ORDER BY CASE a.status "
            + "  WHEN 'ACTIVE' THEN 1 "
            + "  WHEN 'WAITING' THEN 2 "
            + "  ELSE 3 "
            + "END, a.start_time ASC";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        AuctionDTO auction = mapResultSetToAuctionDTO(rs);
        auctions.add(auction);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return auctions;
  }

  public List<AuctionDTO> findAuctionsBySellerId(String sellerId) {
    List<AuctionDTO> auctions = new ArrayList<>();
    String sql = "SELECT a.id, a.start_time, a.end_time, a.status, a.current_price, i.name AS item_name "
            + "FROM auctions a "
            + "JOIN items i ON a.item_id = i.id "
            + "WHERE a.seller_id = ? "
            + "ORDER BY CASE a.status "
            + "  WHEN 'ACTIVE' THEN 1 "
            + "  WHEN 'WAITING' THEN 2 "
            + "  WHEN 'CLOSED' THEN 3 "
            + "  ELSE 4 "
            + "END, a.start_time ASC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      // Gán giá trị userId vào dấu ? trong câu lệnh SQL
      ps.setString(1, sellerId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          AuctionDTO auction = mapResultSetToAuctionDTO(rs);
          auctions.add(auction);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return auctions;
  }

  // Lấy tất cả các phiên đấu giá đang mở thuộc về một seller
  public List<AuctionDTO> findActiveAuctionsBySellerId(String sellerId) {
    List<AuctionDTO> auctions = new ArrayList<>();
    // Cập nhật câu SQL: Thêm LEFT JOIN với bảng users và lấy cột real_name
    String sql = "SELECT a.id, a.start_time, a.end_time, a.status, a.current_price, i.name AS item_name "
        + "FROM auctions a "
        + "JOIN items i ON a.item_id = i.id "
        + "WHERE a.status = 'ACTIVE' AND a.seller_id = ?"
        + "ORDER BY a.start_time ASC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      // Gán giá trị userId vào dấu ? trong câu lệnh SQL
      ps.setString(1, sellerId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          AuctionDTO auction = mapResultSetToAuctionDTO(rs);
          auctions.add(auction);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return auctions;
  }

  // Cập nhật giá cao nhất khi có người bid
  public boolean cancelActiveAndWaitingAuctionsBySellerId(String sellerId) {
    String sql = "UPDATE auctions SET status = ? WHERE seller_id = ? AND status IN (?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, AuctionStatus.CANCELED.name());
      ps.setString(2, sellerId);
      ps.setString(3, AuctionStatus.ACTIVE.name());
      ps.setString(4, AuctionStatus.WAITING.name());
      ps.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean restoreCanceledAuctionsBySellerId(String sellerId, LocalDateTime now) {
    String sql = "UPDATE auctions SET status = CASE "
        + "WHEN end_time <= ? THEN ? "
        + "WHEN start_time <= ? AND end_time > ? THEN ? "
        + "WHEN start_time > ? THEN ? "
        + "ELSE status END "
        + "WHERE seller_id = ? AND status = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      Timestamp currentTimestamp = Timestamp.valueOf(now);
      ps.setTimestamp(1, currentTimestamp);
      ps.setString(2, AuctionStatus.CLOSED.name());
      ps.setTimestamp(3, currentTimestamp);
      ps.setTimestamp(4, currentTimestamp);
      ps.setString(5, AuctionStatus.ACTIVE.name());
      ps.setTimestamp(6, currentTimestamp);
      ps.setString(7, AuctionStatus.WAITING.name());
      ps.setString(8, sellerId);
      ps.setString(9, AuctionStatus.CANCELED.name());
      ps.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
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
    item.setId(rs.getString("item_id"));
    item.setName(rs.getString("item_name"));
    item.setType(itemType);
    item.setDescription(rs.getString("item_description"));
    AuctionResponseDTO auction = new AuctionResponseDTO();
    auction.setId(rs.getString("id"));
    auction.setSellerId(rs.getString("seller_id"));
    auction.setItem(item);
    auction.setCurrentHighestPrice(rs.getBigDecimal("current_price"));
    auction.setMinStepPrice(rs.getBigDecimal("min_step_price"));
    auction.setStatus(AuctionStatus.valueOf(rs.getString("status")));
    auction.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
    auction.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());

    UserRepository userRepo = new  UserRepository();
    String highestBidderId = rs.getString("highest_bidder_id");
    String highestBidderName = userRepo.getAccountNameByUserId(rs.getString("highest_bidder_id"));

    String sellerId = rs.getString("seller_id");
    auction.setUserId(sellerId);

    auction.setHighestBidderId(highestBidderId);
    auction.setHighestBidderName(highestBidderName);

    // Đọc đường dẫn ảnh sản phẩm
    auction.setImagePath(rs.getString("image_path"));

    return auction;
  }

  private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {
    String typeStr = rs.getString("item_type");
    ItemType itemType;
    try {
      itemType = (typeStr != null) ? ItemType.valueOf(typeStr.toUpperCase()) : ItemType.OTHER;
    } catch (IllegalArgumentException e) {
      // Mặc định là OTHER nếu dưới Database lỡ nhập sai chữ
      itemType = ItemType.OTHER;
    }
    Item item = new Item();
    item.setId(rs.getString("item_id"));
    item.setName(rs.getString("item_name"));
    item.setType(itemType);
    item.setDescription(rs.getString("item_description"));
    Auction auction = new Auction();
    auction.setId(rs.getString("id"));
    auction.setSellerId(rs.getString("seller_id"));
    auction.setItem(item);
    auction.setCurrentHighestPrice(rs.getBigDecimal("current_price"));
    auction.setMinStepPrice(rs.getBigDecimal("min_step_price"));
    auction.setStatus(AuctionStatus.valueOf(rs.getString("status")));
    auction.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
    auction.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());

    UserRepository userRepo = new  UserRepository();
    String highestBidderId = rs.getString("highest_bidder_id");
    String highestBidderName = rs.getString("highest_bidder_name");

    String sellerId = rs.getString("seller_id");

    auction.setHighestBidderId(highestBidderId);
    auction.setHighestBidderName(highestBidderName);

    return auction;
  }

  public boolean saveAuction(Auction auction, String imagePath) {
    String sql = "INSERT INTO auctions (id, seller_id, item_id, start_price, min_step_price, current_price, highest_bidder_id, start_time, end_time, status, image_path) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, auction.getId());
      ps.setString(2, auction.getSellerId());
      ps.setString(3, auction.getItem().getId());
      ps.setBigDecimal(4, auction.getStartPrice());
      ps.setBigDecimal(5, auction.getMinStepPrice());
      ps.setBigDecimal(6, auction.getCurrentHighestPrice());
      ps.setString(7, auction.getHighestBidderId());
      ps.setTimestamp(8, java.sql.Timestamp.valueOf(auction.getStartTime()));
      ps.setTimestamp(9, java.sql.Timestamp.valueOf(auction.getEndTime()));
      ps.setString(10, auction.getStatus().name());
      ps.setString(11, imagePath);

      return ps.executeUpdate() > 0;

    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
  public void closeExpiredAuctions(List<String> ids) {

    String sql =
        "UPDATE auctions SET status='CLOSED' WHERE id=?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      for (String id : ids) {
        ps.setString(1, id);
        ps.executeUpdate();

      }

    } catch (Exception e) {
      e.printStackTrace();
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
      e.printStackTrace();
      return false;
    }
  }
  public List<String> findAuctionsToActivate(LocalDateTime now) {

    List<String> ids = new ArrayList<>();

    String sql =
        "SELECT id FROM auctions "
            + "WHERE status='WAITING' "
            + "AND start_time <= ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setTimestamp(1, Timestamp.valueOf(now));

      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        ids.add(rs.getString("id"));
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return ids;
  }
  public List<String> findAuctionsToClose(LocalDateTime now) {

    List<String> ids = new ArrayList<>();

    String sql =
        "SELECT id FROM auctions "
            + "WHERE status='ACTIVE' AND end_time <= ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setTimestamp(1, Timestamp.valueOf(now));

      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        ids.add(rs.getString("id"));
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return ids;
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
      e.printStackTrace();
    }
  }
  public Auction findAuctionById(String auctionId) {
    // Cập nhật câu SQL tương tự như trên
    String sql = "SELECT a.*, i.name AS item_name, i.type AS item_type, i.description AS item_desc, u.real_name AS highest_bidder_name "
            + "FROM auctions a "
            + "JOIN items i ON a.item_id = i.id "
            + "LEFT JOIN users u ON a.highest_bidder_id = u.id "
            + "WHERE a.id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, auctionId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return mapResultSetToAuction(rs);
      }
      return null;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public AuctionResponseDTO findAuctionResponseDTOById(String auctionId) {
    // Cập nhật câu SQL tương tự như trên
    String sql = "SELECT a.*, i.name AS item_name, i.type AS item_type, i.description AS item_desc, u.real_name AS highest_bidder_name "
        + "FROM auctions a "
        + "JOIN items i ON a.item_id = i.id "
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
        "SELECT a.*, i.id AS item_id, i.name AS item_name, i.type AS item_type, i.description AS item_desc "
            + "FROM auctions a "
            + "JOIN items i ON a.item_id = i.id "
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
      e.printStackTrace();
    }

    return result;
  }
  public String getSellerIdByAuctionId(String auctionId) {
    String sql = "SELECT seller_id FROM auctions WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, auctionId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString("seller_id");
      }
      return null;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public String getItemIdByAuctionId(String auctionId) {
    String sql = "SELECT item_id FROM auctions WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, auctionId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString("item_id");
      }
      return null;
    } catch (SQLException e) {
      e.printStackTrace();
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
    String sql = "SELECT a.*, i.name AS item_name, i.type AS item_type, i.description AS item_desc, u.real_name AS highest_bidder_name "
        + "FROM auctions a "
        + "JOIN items i ON a.item_id = i.id "
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
}

