package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;

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
   * @param walletId mã định danh duy nhất của ví tiền (thường được tạo ngẫu nhiên)
   * @param userId   mã định danh của người dùng sẽ sở hữu chiếc ví này
   * @return true nếu việc chèn dữ liệu ví vào hệ thống thành công,
   * @code false nếu có lỗi xảy ra
   */
  public boolean createWallet(Connection conn,
                              String walletId,
                              String userId) {

    String sql = "INSERT INTO wallets (id, user_id) VALUES (?, ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, walletId);
      ps.setString(2, userId);

      return ps.executeUpdate() > 0;

    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }
}
