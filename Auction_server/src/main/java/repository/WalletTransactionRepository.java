package repository;

import com.auction.shared.model.transaction.WalletTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.auction.shared.enums.WalletTransactionStatus;
import com.auction.shared.enums.WalletTransactionType;
import java.math.BigDecimal;

public class WalletTransactionRepository {
  private WalletTransactionRepository() {}

  private static class Holder {
    private static final WalletTransactionRepository INSTANCE = new WalletTransactionRepository();
  }

  public static WalletTransactionRepository getInstance() {
    return Holder.INSTANCE;
  }
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

  public List<WalletTransaction> findPendingTransactions(Connection conn) {
    String sql = "SELECT * FROM wallet_transactions WHERE status = ?";
    List<WalletTransaction> transactions = new ArrayList<>();
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, WalletTransactionStatus.PENDING.name());
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          transactions.add(mapResultSetToTransaction(rs));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return transactions;
  }

  public WalletTransaction getTransactionById(Connection conn, String id) {
    String sql = "SELECT * FROM wallet_transactions WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToTransaction(rs);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  public boolean updateWalletTransaction(Connection conn, WalletTransaction walletTransaction) {
    String sql = "UPDATE wallet_transactions SET status = ? WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, walletTransaction.getStatus().name());
      ps.setString(2, walletTransaction.getId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private WalletTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
    WalletTransaction tx = WalletTransaction.builder()
        .walletId(rs.getString("wallet_id"))
        .type(WalletTransactionType.valueOf(rs.getString("type")))
        .amount(rs.getBigDecimal("amount"))
        .referenceId(rs.getString("reference_id"))
        .status(WalletTransactionStatus.valueOf(rs.getString("status")))
        .build();
    tx.setId(rs.getString("id"));
    return tx;
  }
}
