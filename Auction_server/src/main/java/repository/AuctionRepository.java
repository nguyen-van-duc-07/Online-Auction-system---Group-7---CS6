package repository;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.enums.ItemType;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.item.Item;
import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuctionRepository {
  // Lấy tất cả các phiên đấu giá đang mở
  public List<Auction> findActiveAuctions() {
    List<Auction> auctions = new ArrayList<>();
    String sql = "SELECT a.*, i.name AS item_name, i.type AS item_type, i.description AS item_desc "
        + "FROM auctions a "
        + "JOIN items i ON a.item_id = i.id "
        + "WHERE a.status = 'ACTIVE'";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        // Chuyển đổi dữ liệu từ DB sang Object
        Auction auction = mapResultSetToAuction(rs);
        auctions.add(auction);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return auctions;
  }

  // Cập nhật giá cao nhất khi có người bid
  public void updatePrice(String auctionId, java.math.BigDecimal newPrice, String bidderId) {
    String sql = "UPDATE auctions SET current_price = ?, highest_bidder_id = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setBigDecimal(1, newPrice);
      ps.setString(2, bidderId);
      ps.setString(3, auctionId);
      ps.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  // Hàm phụ trợ để mapping dữ liệu (tránh viết lặp code)
  private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {
    String typeStr = rs.getString("item_type");
    ItemType itemType;
    try {
      itemType = (typeStr != null) ? ItemType.valueOf(typeStr.toUpperCase()) : ItemType.OTHER;
    } catch (IllegalArgumentException e) {
      // Mặc định là OTHER nếu dưới Database lỡ nhập sai chữ
      itemType = ItemType.OTHER;
    }
    Item item = new Item(rs.getString("item_id"),
        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
        rs.getString("item_name"),   // Lấy từ AS item_name của lệnh JOIN
        itemType,                    // Enum vừa xử lý ở trên
        rs.getString("item_desc"));
    Auction a = new Auction(
        item,
        rs.getBigDecimal("start_price"),
        rs.getTimestamp("start_time").toLocalDateTime(),
        rs.getTimestamp("end_time").toLocalDateTime()
    );
    a.setId(rs.getString("id"));
    a.setCurrentHighestPrice(rs.getBigDecimal("current_price"));
    a.setStatus(AuctionStatus.valueOf(rs.getString("status")));
    a.setHighestBidderId(rs.getString("highest_bidder_id"));
    return a;
  }
  public void saveAuction(Auction auction, String sellerId) {
    String sql = "INSERT INTO auctions (id, seller_id, item_id, start_price, current_price, start_time, end_time, status) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, auction.getId());
      ps.setString(2, sellerId);
      ps.setString(3, auction.getItem().getId());
      ps.setBigDecimal(4, auction.getStartPrice());
      ps.setBigDecimal(5, auction.getCurrentHighestPrice());
      ps.setTimestamp(6, java.sql.Timestamp.valueOf(auction.getStartTime()));
      ps.setTimestamp(7, java.sql.Timestamp.valueOf(auction.getEndTime()));
      ps.setString(8, auction.getStatus().name());

      ps.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
