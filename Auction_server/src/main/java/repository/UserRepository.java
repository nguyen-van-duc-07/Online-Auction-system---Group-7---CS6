package repository;

import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.Admin;
import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.InfoDTO;
import com.auction.shared.model.user.User;
import config.DatabaseConnection;

import java.sql.*;

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
  public static String getPasswordByAccountName(String accountName) {
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

  public static boolean isAccountExist(String accountName) {
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
   * @param conn đối tượng kết nối cơ sở dữ liệu đang mở
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

  /**
   * Lấy thông tin người dùng dựa trên accountName hoặc id.
   * Nếu accountName khác null, sẽ ưu tiên tìm theo account_name.
   * Nếu accountName là null, sẽ tìm theo id.
   *
   * @param accountName tên tài khoản người dùng (có thể null)
   * @param id          mã định danh người dùng (có thể null)
   * @return đối tượng User nếu tìm thấy, ngược lại trả về null
   */
  public User getUserByAccountNameOrId(String accountName, String id) {
    // Nếu cả hai đều null thì không cần thực hiện truy vấn
    if (accountName == null && id == null) {
      return null;
    }

    try (Connection conn = DatabaseConnection.getConnection()) {
      // Cập nhật câu lệnh SQL để tìm kiếm theo account_name hoặc id
      String sql;
      if (accountName != null) {
        sql = "SELECT * FROM users WHERE account_name = ?";
      } else {
        sql = "SELECT * FROM users WHERE id = ?";
      }

      PreparedStatement ps = conn.prepareStatement(sql);

      if (accountName != null) {
        ps.setString(1, accountName);
      } else {
        ps.setString(1, id);
      }

      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        User user = null;
        UserRole userRole = UserRole.valueOf(rs.getString("role"));
        if (userRole == UserRole.BIDDER) {
          user = new Bidder();
        } else {
          user = new Admin();
        }
        user.setRole(userRole);
        user.setId(rs.getString("id"));
        user.setAccountName(rs.getString("account_name"));
        user.setRealName(rs.getString("real_name"));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setAddress(rs.getString("address"));
        java.sql.Date dobDate = rs.getDate("dob");
        if (dobDate != null) {
          user.setDob(dobDate.toLocalDate()); // Chuyển java.sql.Date -> java.time.LocalDate
        }

        return user;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Lấy tên hiển thị của người dùng dựa trên ID.
   * Ưu tiên lấy tên thật (real_name), nếu chưa cập nhật hồ sơ thì dùng tên tài khoản (account_name).
   * Phục vụ cho việc hiển thị tên người chiến thắng trên giao diện đấu giá.
   *
   * @param userId mã định danh (id) của người dùng
   * @return Tên hiển thị của người dùng, hoặc "Người dùng ẩn danh" nếu có lỗi/không tìm thấy
   */
  public static String getUserFullName(String userId) {
    if (userId == null || userId.trim().isEmpty()) {
      return "Người dùng ẩn danh";
    }

    // Chỉ Select đúng 2 cột cần thiết để tối ưu tốc độ, thay vì Select *
    String sql = "SELECT real_name, account_name FROM users WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, userId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String realName = rs.getString("real_name");
          String accountName = rs.getString("account_name");

          // Nếu có tên thật và không bị rỗng thì dùng tên thật
          if (realName != null && !realName.trim().isEmpty()) {
            return realName;
          }
          // Nếu không, trả về tên tài khoản
          else if (accountName != null) {
            return accountName;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(">>> Lỗi khi truy vấn tên người dùng: " + e.getMessage());
    }

    // Trả về mặc định nếu Database lỗi để tránh làm sập luồng hiển thị (NullPointerException)
    return "Người dùng ẩn danh";
  }

  public boolean updateProfile(User user) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "UPDATE users "
          + "SET real_name = ?, dob = ?, email = ?, phone_number = ?, address = ? "
          + " WHERE id = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, user.getRealName());
      ps.setDate(2, Date.valueOf(user.getDob()));
      ps.setString(3, user.getEmail());
      ps.setString(4, user.getPhoneNumber());
      ps.setString(5, user.getAddress());
      ps.setString(6, user.getId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean deleteAccount(User user) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "DELETE FROM users "
          + "WHERE id = ?;";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, user.getId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException();
    }
  }

  /**
   * Lấy tên thật (real_name) của người dùng dựa trên mã định danh (userId).
   * * @param userId Mã định danh của người dùng cần tra cứu.
   *
   * @return Tên thật của người dùng nếu tìm thấy, ngược lại trả về null.
   */
  public String getRealNameByUserId(String userId) {
    String query = "SELECT real_name FROM users WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

      pstmt.setString(1, userId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("real_name");
        }
      }
    } catch (SQLException e) {
      System.err.println("Lỗi khi lấy real_name của userId: " + userId);
      e.printStackTrace();
    }

    return null;
  }

  public InfoDTO getInfoByUserId(String userId) {
    String query = "SELECT real_name, phone_number, address "
        + "FROM users "
        + "WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

      pstmt.setString(1, userId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          InfoDTO infoDTO = new InfoDTO();
          infoDTO.setConsigneeName(rs.getString("real_name"));
          infoDTO.setPhoneNumber(rs.getString("phone_number"));
          infoDTO.setAddress(rs.getString("address"));
          return infoDTO;
        }
      }
    } catch (SQLException e) {
      System.err.println("Lỗi khi lấy thông tin của userId: " + userId);
      e.printStackTrace();
    }
    return null;
  }
}
