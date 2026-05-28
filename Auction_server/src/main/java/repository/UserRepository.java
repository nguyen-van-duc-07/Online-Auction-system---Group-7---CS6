package repository;

import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.Admin;
import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.InfoDTO;
import com.auction.shared.model.user.User;
import config.DatabaseConnection;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lớp UserRepository dùng để thao tác với bảng users trong cơ sở dữ liệu.
 * Cung cấp các phương thức truy vấn và xử lý dữ liệu người dùng.
 */
public class UserRepository {
  private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

  /**
   * Lấy mật khẩu của người dùng theo tên tài khoản.
   *
   * @param phoneNumber so dien thoai nguoi dung
   * @return mật khẩu tương ứng nếu tìm thấy, ngược lại trả về null
   */
  public String getPasswordByPhoneNumber(String phoneNumber) {
    try (Connection conn = DatabaseConnection.getConnection()) {

      String sql = "SELECT password FROM users WHERE phone_number = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, phoneNumber);

      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        return rs.getString("password");
      }

    } catch (Exception e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy mật khẩu của số điện thoại: {}", phoneNumber, e);
    }

    return null;
  }

  public boolean isAccountExist(String phoneNumber) {
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
      log.error("Lỗi cơ sở dữ liệu khi kiểm tra sự tồn tại của số điện thoại: {}", phoneNumber, e);
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
      log.error("Lỗi cơ sở dữ liệu khi tạo người dùng ID: {}", user.getId(), e);
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
      log.error("Lỗi cơ sở dữ liệu khi lấy thông tin người dùng bằng sđt: {} hoặc ID: {}", phoneNumber, id, e);
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
      log.error("Lỗi cơ sở dữ liệu khi cập nhật hồ sơ người dùng ID: {}", user.getId(), e);
      throw new RuntimeException(e);
    }
  }

  public boolean deleteAccount(User user) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "DELETE FROM users "
          + "WHERE id = ?;";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, user.getId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi xóa tài khoản người dùng ID: {}", user.getId(), e);
      throw new RuntimeException(e);
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
      log.error("Lỗi cơ sở dữ liệu khi lấy account_name của user ID: {}", userId, e);
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
      log.error("Lỗi cơ sở dữ liệu khi lấy thông tin giao hàng của user ID: {}", userId, e);
    }
    return null;
  }
  public String getPasswordByUserId(String userId) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "SELECT password FROM users WHERE id = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, userId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString("password");
      }
    } catch (Exception e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy mật khẩu của user ID: {}", userId, e);
    }
    return null;
  }

  public boolean updatePassword(String userId, String hashedPassword) {
    String sql = "UPDATE users SET password = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, hashedPassword);
      ps.setString(2, userId);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi cập nhật mật khẩu cho user ID: {}", userId, e);
    }
    return false;
  }

  public java.util.List<com.auction.shared.model.user.UserDTO> getAllUsers() {
    java.util.List<com.auction.shared.model.user.UserDTO> list = new java.util.ArrayList<>();
    String sql = "SELECT id, account_name, dob, phone_number, email, address, role FROM users";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        com.auction.shared.model.user.UserDTO dto = new com.auction.shared.model.user.UserDTO();
        dto.setId(rs.getString("id"));
        dto.setAccountName(rs.getString("account_name"));
        dto.setEmail(rs.getString("email"));
        dto.setPhoneNumber(rs.getString("phone_number"));
        dto.setAddress(rs.getString("address"));
        
        String roleStr = rs.getString("role");
        if (roleStr != null) {
          dto.setRole(com.auction.shared.enums.UserRole.valueOf(roleStr));
        }
        
        java.sql.Date dobDate = rs.getDate("dob");
        if (dobDate != null) {
          dto.setDob(dobDate.toLocalDate());
        }
        list.add(dto);
      }
    } catch (Exception e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy toàn bộ danh sách người dùng", e);
    }
    return list;
  }

  public boolean saveAdminAccount(Admin admin) {
    String sql = "INSERT INTO users (id, account_name, password, dob, email, phone_number, address, role) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, admin.getId());
      ps.setString(2, admin.getAccountName());
      ps.setString(3, admin.getPassword());
      if (admin.getDob() != null) {
        ps.setDate(4, Date.valueOf(admin.getDob()));
      } else {
        ps.setNull(4, java.sql.Types.DATE);
      }
      ps.setString(5, admin.getEmail());
      ps.setString(6, admin.getPhoneNumber());
      ps.setString(7, admin.getAddress());
      ps.setString(8, admin.getRole() != null ? admin.getRole().toString() : "ADMIN");
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lưu tài khoản admin ID: {}", admin.getId(), e);
      throw new RuntimeException(e);
    }
  }
}
