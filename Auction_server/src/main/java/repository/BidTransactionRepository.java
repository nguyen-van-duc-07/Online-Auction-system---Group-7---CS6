package repository;

import com.auction.shared.model.transaction.BidTransaction;
import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BidTransactionRepository {
  private static final Logger log = LoggerFactory.getLogger(BidTransactionRepository.class);

  private BidTransactionRepository() {}

  private static class Holder {
    private static final BidTransactionRepository INSTANCE = new BidTransactionRepository();
  }

  public static BidTransactionRepository getInstance() {
    return Holder.INSTANCE;
  }
  public boolean saveBid(Connection conn, BidTransaction bid) throws SQLException {
    String sql = "INSERT INTO bid_transactions "
        + "(id, auction_id, bidder_id, bid_amount) "
        + "VALUES (?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, bid.getId());
      ps.setString(2, bid.getAuctionId());
      ps.setString(3, bid.getBidderId());
      ps.setString(4, String.valueOf(bid.getBidAmount()));
      return ps.executeUpdate() > 0;
    }
  }
  // BidTransactionRepository.java
  public List<BidTransaction> findRecentByAuctionId(String auctionId, int limit) {
    List<BidTransaction> list = new ArrayList<>();
    // SQL: Sắp xếp theo thời gian giảm dần (mới nhất lên đầu) và giới hạn số lượng
    String sql = "SELECT * FROM bid_transactions WHERE auction_id = ? ORDER BY created_at DESC LIMIT ?";

    // Sử dụng try-with-resources để tự động đóng Connection sau khi dùng (tránh rò rỉ bộ nhớ)
    try (Connection conn = DatabaseConnection.getConnection()) {

      // KIỂM TRA PHÒNG THỦ: Tránh NullPointerException
      if (conn == null) {
        log.error(">>> LỖI: Không thể kết nối Database!");
        return list;
      }

      try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, auctionId);
        ps.setInt(2, limit);

        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            list.add(new BidTransaction(
                    rs.getString("auction_id"),
                    rs.getString("bidder_id"),
                    rs.getBigDecimal("bid_amount")
            ));
          }
        }
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy danh sách bid gần đây của phiên ID: {}", auctionId, e);
    }
    return list;
  }

  /**
   * Xóa toàn bộ lịch sử đặt giá của danh sách các phiên đấu giá được chỉ định.
   * Phương thức này nhận Connection từ bên ngoài để có thể được gọi trong một Transaction chung.
   */
  public boolean deleteByAuctionIds(Connection conn, List<String> auctionIds) throws SQLException {
    if (auctionIds == null || auctionIds.isEmpty()) return true;

    StringBuilder sql = new StringBuilder("DELETE FROM bid_transactions WHERE auction_id IN (");
    for (int i = 0; i < auctionIds.size(); i++) {
      sql.append("?");
      if (i < auctionIds.size() - 1) sql.append(", ");
    }
    sql.append(")");

    try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
      for (int i = 0; i < auctionIds.size(); i++) {
        ps.setString(i + 1, auctionIds.get(i));
      }
      int affected = ps.executeUpdate();
      log.info("[RESTORE] Đã xóa {} bản ghi lịch sử đặt giá cũ của các phiên khôi phục.", affected);
      return true;
    }
  }
}
