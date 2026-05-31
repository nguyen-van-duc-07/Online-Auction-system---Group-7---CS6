package repository;

import com.auction.shared.model.user.Wallet;
import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lớp WalletRepository dùng để thao tác với bảng wallets trong cơ sở dữ liệu.
 * Cung cấp các phương thức truy vấn và xử lý dữ liệu liên quan đến ví điện tử
 * (tài khoản thanh toán) của người dùng trong hệ thống đấu giá.
 */
public class WalletRepository {
  private static final Logger log = LoggerFactory.getLogger(WalletRepository.class);

  private WalletRepository() {}

  private static class Holder {
    private static final WalletRepository INSTANCE = new WalletRepository();
  }

  public static WalletRepository getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * Tạo một ví điện tử mới và liên kết nó với tài khoản người dùng.
   * Phương thức này nhận một đối tượng {@link Connection} từ bên ngoài truyền vào.
   * Điều này đặc biệt quan trọng vì nó cho phép thao tác tạo ví nằm chung trong
   * một Transaction với các thao tác khác (như tạo User), giúp đảm bảo tính toàn
   * vẹn của dữ liệu nếu xảy ra lỗi.
   *
   * @param conn     đối tượng kết nối cơ sở dữ liệu đang mở (hỗ trợ Transaction)
   * @param wallet nhận được từ AuthService
   * @return true nếu việc chèn dữ liệu ví vào hệ thống thành công,
   * @code false nếu có lỗi xảy ra
   */
  public boolean createWallet(Connection conn, Wallet wallet) {

    String sql = "INSERT INTO wallets (id, user_id) VALUES (?, ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, wallet.getId());
      ps.setString(2, wallet.getBidderId());

      return ps.executeUpdate() > 0;

    } catch (Exception e) {
      log.error("Lỗi cơ sở dữ liệu khi tạo ví cho user ID: {}", wallet.getBidderId(), e);
    }

    return false;
  }

  public Wallet getWalletByUserId(String userId) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "SELECT * FROM wallets WHERE user_id = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, userId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return mapRow(rs);
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy ví của user ID: {}", userId, e);
      throw new RuntimeException(e);
    }
    return null;
  }

  public boolean updateWallet(Wallet wallet) {
    String sql = "UPDATE wallets "
        + "SET balance = ?, "
        + "frozen_balance = ? "
        + "WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
    PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, String.valueOf(wallet.getBalance()));
      ps.setString(2, String.valueOf(wallet.getFrozenBalance()));
      ps.setString(3, wallet.getId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi cập nhật ví ID: {}", wallet.getId(), e);
      throw new RuntimeException(e);
    }
  }
  public Wallet getWalletByUserIdForUpdate(Connection conn, String userId) {
    String sql = "SELECT * FROM wallets WHERE user_id = ? FOR UPDATE";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, userId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return mapRow(rs);
      }
      return null;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi khóa ví để cập nhật cho user ID: {}", userId, e);
      throw new RuntimeException(e);
    }
  }

  public Wallet getWalletByWalletId(Connection conn, String walletId) {
    String sql = "SELECT * FROM wallets WHERE id = ? FOR UPDATE";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, walletId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return mapRow(rs);
      }
      return null;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi khóa ví để cập nhật cho ví ID: {}", walletId, e);
      throw new RuntimeException(e);
    }
  }

  private Wallet mapRow(ResultSet rs) throws SQLException {
    Wallet wallet = new Wallet();

    wallet.setId(rs.getString("id"));
    wallet.setBidderId(rs.getString("user_id"));
    wallet.setBalance(rs.getBigDecimal("balance"));
    wallet.setFrozenBalance(rs.getBigDecimal("frozen_balance"));

    return wallet;
  }
  public boolean updateWallet(Connection conn, Wallet wallet) {
    String sql = "UPDATE wallets "
        + "SET balance = ?, "
        + "frozen_balance = ? "
        + "WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, String.valueOf(wallet.getBalance()));
      ps.setString(2, String.valueOf(wallet.getFrozenBalance()));
      ps.setString(3, wallet.getId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi cập nhật ví ID: {}", wallet.getId(), e);
      throw new RuntimeException(e);
    }
  }
}
