package service;

import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.User;
import com.auction.shared.request.SignUpRequestDTO;
import com.auction.shared.request.LoginRequestDTO;
import org.mindrot.jbcrypt.BCrypt;
import repository.UserRepository;

/**s
 * Lớp AuthService xử lý các chức năng xác thực người dùng.
 * Bao gồm đăng nhập, kiểm tra thông tin tài khoản và xử lý logic liên quan đến authentication.
 */
public class AuthService {

  private static UserRepository userRepo = new UserRepository();

  /**
   * Xử lý đăng nhập người dùng bằng tên tài khoản và mật khẩu.
   *
   * <p>Hệ thống sẽ lấy mật khẩu đã mã hóa từ database theo accountName,
   * sau đó so sánh với mật khẩu người dùng nhập bằng BCrypt.</p>
   *
   * @param loginUser đối tượng {@link LoginRequestDTO} chứa thông tin đăng ký
   * @return true nếu đăng nhập thành công, false nếu thất bại
   */
  public static boolean login(LoginRequestDTO loginUser) {
    String hashedPassword = userRepo.getPasswordByAccountName(loginUser.getAccountName());

    if (hashedPassword == null) {
      return false;
    }

    // Kiểm tra mật khẩu và mật khẩu đã mã hoá bằng thư viện BCrypt xem có giống nhau không
    return BCrypt.checkpw(loginUser.getPassword(), hashedPassword);
  }

  /**
   * Xử lý đăng ký tài khoản mới cho người dùng.
   * Luồng xử lý bao gồm:
   * Kiểm tra xem tên tài khoản đã tồn tại trong cơ sở dữ liệu chưa.</li>
   * Mã hóa an toàn mật khẩu bằng thuật toán BCrypt.</li>
   * Gửi tên và tài khoản đã băm xuống Database để lưu trữ
   *
   * @param signUpUser đối tượng {@link SignUpRequestDTO} chứa thông tin đăng ký
   * @return {@code true} nếu quá trình tạo tài khoản thành công,
   * {@code false} nếu tài khoản đã tồn tại hoặc xảy ra lỗi hệ thống
   */
  public static boolean signUp(SignUpRequestDTO signUpUser) {
    // Kiểm tra xem tài khoản đã tồn tại trong Database chưa
    if (userRepo.isAccountExist(signUpUser.getAccountName())) {
      return false; // Tài khoản đã tồn tại trong Database
    }

    String hashedPassword = BCrypt.hashpw(signUpUser.getPassword(), BCrypt.gensalt());

    User newUser = new Bidder();
    newUser.setId(java.util.UUID.randomUUID().toString());
    newUser.setAccountName(signUpUser.getAccountName());
    newUser.setPassword(hashedPassword);

    // Lưu cả đối tượng Entity xuống Repo
    return userRepo.createUser(newUser);
  }
}