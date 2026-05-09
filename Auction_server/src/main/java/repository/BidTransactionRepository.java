package repository;

import com.auction.shared.model.transaction.BidTransaction;
import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
