package service;

import com.auction.shared.model.user.User;
import org.mindrot.jbcrypt.BCrypt;
import repository.UserRepository;

/**
 * Lớp AuthService xử lý các chức năng xác thực người dùng.
 * Bao gồm đăng nhập, kiểm tra thông tin tài khoản và xử lý logic liên quan đến authentication.
 */
public class AuthService {

  private static UserRepository repo = new UserRepository();

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
  public static boolean login(String accountName, String password) {
    String hashedPassword = repo.getPasswordByAccountName(accountName);

    if (hashedPassword == null) {
      return false;
    }

    // Kiểm tra mật khẩu và mật khẩu đã mã hoá bằng thư viện BCrypt xem có giống nhau không
    return BCrypt.checkpw(password, hashedPassword);
  }

  public static boolean signup(User signupUser) {
    // Kiểm tra xem tài khoản đã tồn tại trong Database chưa
    if (repo.isAccountExist(signupUser.getUserName())) {
      return false; // Tài khoản đã tồn tại trong Database
    }

    // Băm mật khẩu bằng thư viện BCrypt để bảo mật
    // Hàm gensalt() sẽ tự động tạo ra một chuỗi nhiễu ngẫu nhiên để trộn vào mật khẩu
    String hashedPassword = BCrypt.hashpw(signupUser.getPassword(), BCrypt.gensalt());

    // Gửi tên và tài khoản đã băm xuống Database để lưu trữ
    return repo.createUser(signupUser.getId(), signupUser.getUserName(), hashedPassword);
  }
}