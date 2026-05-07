package repository;

import com.auction.shared.model.user.Wallet;
import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Lớp WalletRepository dùng để thao tác với bảng wallets trong cơ sở dữ liệu.
 * Cung cấp các phương thức truy vấn và xử lý dữ liệu liên quan đến ví điện tử
 * (tài khoản thanh toán) của người dùng trong hệ thống đấu giá.
 */
public class WalletRepository {

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
      e.printStackTrace();
    }

    return false;
  }
  public static boolean deleteWallet(Wallet wallet) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "DELETE FROM wallets WHERE id = ?";

      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, wallet.getId());
      return ps.executeUpdate() > 0;

    } catch (Exception e) {
      e.printStackTrace();
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
        Wallet wallet = new Wallet();
        wallet.setId(rs.getString("id"));
        wallet.setBidderId(rs.getString("user_id"));
        wallet.setBalance(rs.getBigDecimal("balance"));
        wallet.setFrozenBalance(rs.getBigDecimal("frozen_balance"));
        return wallet;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return null;
  }
}
