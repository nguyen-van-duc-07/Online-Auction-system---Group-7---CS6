package service;

import org.mindrot.jbcrypt.BCrypt;
import repository.UserRepository;

/**
 * Lớp AuthService xử lý các chức năng xác thực người dùng.
 * Bao gồm đăng nhập, kiểm tra thông tin tài khoản và xử lý logic liên quan đến authentication.
 */
public class AuthService {

  private final UserRepository repo = new UserRepository();

  /**
   * Xử lý đăng nhập người dùng bằng tên tài khoản và mật khẩu.
   *
   * <p>Hệ thống sẽ lấy mật khẩu đã mã hóa từ database theo accountName,
   * sau đó so sánh với mật khẩu người dùng nhập bằng BCrypt.</p>
   *
   * @param accountName tên tài khoản người dùng
   * @param password mật khẩu người dùng nhập vào (dạng plain text)
   * @return true nếu đăng nhập thành công, false nếu thất bại
   */
  public boolean login(String accountName, String password) {
    String hashedPassword = repo.getPasswordByAccountName(accountName);

    if (hashedPassword == null) {
      return false;
    }

    return BCrypt.checkpw(password, hashedPassword);
  }
}