package repository;

import com.auction.shared.model.user.User;
import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Lớp UserRepository dùng để thao tác với bảng users trong cơ sở dữ liệu.
 * Cung cấp các phương thức truy vấn và xử lý dữ liệu người dùng.
 */
public class UserRepository {

  /**
   * Lấy mật khẩu của người dùng theo tên tài khoản.
   *
   * @param accountName tên tài khoản người dùng
   * @return mật khẩu tương ứng nếu tìm thấy, ngược lại trả về null
   */
  public String getPasswordByAccountName(String accountName) {
    try (Connection conn = DatabaseConnection.getConnection()) {

      String sql = "SELECT password FROM users WHERE account_name = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, accountName);

      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        return rs.getString("password");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public boolean isAccountExist(String accountName) {
    // Dùng SELECT 1 cho tốc độ truy vấn tối đa
    String sql = "SELECT 1 FROM users WHERE account_name = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, accountName);

      try (ResultSet rs = ps.executeQuery()) {
        // rs.next() sẽ trả về true nếu Database tìm thấy ít nhất 1 dòng khớp tên
        return rs.next();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false; // Mặc định nếu có lỗi hoặc không tìm thấy thì coi như chưa tồn tại
  }

  /**
   * Thêm một người dùng mới vào cơ sở dữ liệu.
   * Phương thức này nhận một đối tượng {@link Connection} từ bên ngoài truyền vào.
   * Việc này rất hữu ích khi bạn muốn đưa thao tác tạo người dùng này vào một
   * Transaction (giao dịch) chung với các thao tác khác.
   *
   * @param conn        đối tượng kết nối cơ sở dữ liệu đang mở
   * @param user object nhận đươ từ AuthService
   * @return {@code true} nếu việc chèn dữ liệu thành công, ngược lại trả về {@code false}
   */
  public boolean createUser(Connection conn, User user) {

    String sql = "INSERT INTO users (id, account_name, password) VALUES (?, ?, ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, user.getId());
      ps.setString(2, user.getAccountName());
      ps.setString(3, user.getPassword());

      return ps.executeUpdate() > 0;

    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  public User getUserByAccountName(String accountName) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "SELECT * FROM users WHERE account_name = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, accountName);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        User user = new com.auction.shared.model.user.Bidder();
        user.setId(rs.getString("id"));
        user.setAccountName(rs.getString("account_name"));
        user.setRealName(rs.getString("real_name"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setDob(rs.getDate("dob").toLocalDate()); // Chuyển java.sql.Date -> java.time.LocalDate

        return user;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
