package repository;

import com.auction.shared.model.transaction.BidTransaction;
import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BidTransactionRepository {
  public boolean saveBid(BidTransaction bid){
    String sql = "INSERT INTO bid_transactions "
        + "(id, auction_id, bidder_id, bid_amount) "
        + "VALUES (?, ?, ?, ?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, bid.getId());
      ps.setString(2, bid.getAuctionId());
      ps.setString(3, bid.getBidderId());
      ps.setString(4, String.valueOf(bid.getBidAmount()));
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  // BidTransactionRepository.java
  public static List<BidTransaction> findRecentByAuctionId(String auctionId, int limit) {
    List<BidTransaction> list = new ArrayList<>();
    // SQL: Sắp xếp theo thời gian giảm dần (mới nhất lên đầu) và giới hạn số lượng
    String sql = "SELECT * FROM bid_transactions WHERE auction_id = ? ORDER BY created_at DESC LIMIT ?";

    // Sử dụng try-with-resources để tự động đóng Connection sau khi dùng (tránh rò rỉ bộ nhớ)
    try (Connection conn = DatabaseConnection.getConnection()) {

      // KIỂM TRA PHÒNG THỦ: Tránh NullPointerException
      if (conn == null) {
        System.err.println(">>> LỖI: Không thể kết nối Database!");
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
      e.printStackTrace();
    }
    return list;
  }
}
