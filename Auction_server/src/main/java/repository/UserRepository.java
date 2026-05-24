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
   * @param phoneNumber so dien thoai nguoi dung
   * @return mật khẩu tương ứng nếu tìm thấy, ngược lại trả về null
   */
  public static String getPasswordByPhoneNumber(String phoneNumber) {
    try (Connection conn = DatabaseConnection.getConnection()) {

      String sql = "SELECT password FROM users WHERE phone_number = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, phoneNumber);

      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        return rs.getString("password");
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public static boolean isAccountExist(String phoneNumber) {
    // Dùng SELECT 1 cho tốc độ truy vấn tối đa
    String sql = "SELECT 1 FROM users WHERE phone_number = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, phoneNumber);

      try (ResultSet rs = ps.executeQuery()) {
        // rs.next() sẽ trả về true nếu Database tìm thấy ít nhất 1 dòng khớp sdt
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

    String sql = "INSERT INTO users (id, account_name, password, phone_number) VALUES (?, ?, ?, ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, user.getId());
      ps.setString(2, user.getAccountName());
      ps.setString(3, user.getPassword());
      ps.setString(4, user.getPhoneNumber());

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
   * @param phoneNumber sdt người dùng (có thể null)
   * @param id          mã định danh người dùng (có thể null)
   * @return đối tượng User nếu tìm thấy, ngược lại trả về null
   */
  public User getUserByPhoneNumberNameOrId(String phoneNumber, String id) {
    // Nếu cả hai đều null thì không cần thực hiện truy vấn
    if (phoneNumber == null && id == null) {
      return null;
    }

    try (Connection conn = DatabaseConnection.getConnection()) {
      // Cập nhật câu lệnh SQL để tìm kiếm theo account_name hoặc id
      String sql;
      if (phoneNumber != null) {
        sql = "SELECT * FROM users WHERE phone_number = ?";
      } else {
        sql = "SELECT * FROM users WHERE id = ?";
      }

      PreparedStatement ps = conn.prepareStatement(sql);

      if (phoneNumber != null) {
        ps.setString(1, phoneNumber);
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

  public boolean updateProfile(User user) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "UPDATE users "
          + "SET account_name = ?, dob = ?, email = ?, address = ? "
          + " WHERE id = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, user.getAccountName());
      ps.setDate(2, Date.valueOf(user.getDob()));
      ps.setString(3, user.getEmail());
      ps.setString(4, user.getAddress());
      ps.setString(5, user.getId());
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
   * Lấy tên của người dùng dựa trên mã định danh (userId).
   * * @param userId Mã định danh của người dùng cần tra cứu.
   *
   * @return Tên của người dùng nếu tìm thấy, ngược lại trả về null.
   */
  public String getAccountNameByUserId(String userId) {
    String query = "SELECT account_name FROM users WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

      pstmt.setString(1, userId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getString("account_name");
        }
      }
    } catch (SQLException e) {
      System.err.println("Lỗi khi lấy account_name của userId: " + userId);
      e.printStackTrace();
    }

    return null;
  }

  public InfoDTO getInfoByUserId(String userId) {
    String query = "SELECT account_name, phone_number, address "
        + "FROM users "
        + "WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

      pstmt.setString(1, userId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          InfoDTO infoDTO = new InfoDTO();
          infoDTO.setConsigneeName(rs.getString("account_name"));
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
