package repository;

import com.auction.shared.model.transaction.WalletTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WalletTransactionRepository {
  public boolean saveWalletTransaction(Connection conn, WalletTransaction walletTransaction) {
    String sql = "INSERT INTO wallet_transactions "
        + "(id, wallet_id, type, amount, reference_id, status) "
        + "VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, walletTransaction.getId());
      ps.setString(2, walletTransaction.getWalletId());
      ps.setString(3, walletTransaction.getType().name());
      ps.setString(4, String.valueOf(walletTransaction.getAmount()));
      ps.setString(5, walletTransaction.getReferenceId());
      ps.setString(6, walletTransaction.getStatus().name());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
