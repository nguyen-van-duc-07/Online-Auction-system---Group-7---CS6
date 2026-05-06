package service;

import com.auction.shared.request.LoginRequestDTO;
import com.auction.shared.request.SignUpRequestDTO;
import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.User;
import com.auction.shared.model.user.Wallet;
import config.DatabaseConnection;
import java.sql.Connection;
import org.mindrot.jbcrypt.BCrypt;
import repository.UserRepository;
import repository.WalletRepository;

/**
 * Lớp AuthService xử lý các chức năng xác thực người dùng.
 * Bao gồm đăng nhập, kiểm tra thông tin tài khoản và xử lý logic liên quan đến authentication.
 */
public class AuthService {

  private static final UserRepository userRepo = new UserRepository();
  private static final WalletRepository walletRepo = new WalletRepository();

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
   * Tạo mã định danh (ID) ngẫu nhiên cho User và Wallet.</li>
   * Mã hóa an toàn mật khẩu bằng thuật toán BCrypt.</li>
   * Mở một Transaction: Thực hiện tuần tự tạo User và tạo Wallet.</li>
   * Nếu quá trình tạo User hoặc Wallet gặp sự cố, toàn bộ tiến trình sẽ
   * bị hoàn tác (rollback) để đảm bảo tính toàn vẹn dữ liệu.
   *
   * @param signUpUser đối tượng {@link SignUpRequestDTO} chứa thông tin đăng ký
   * @return {@code true} nếu quá trình tạo tài khoản và ví thành công,
   * {@code false} nếu tài khoản đã tồn tại hoặc xảy ra lỗi hệ thống
   */
  public static boolean signUp(SignUpRequestDTO signUpUser) {

    if (userRepo.isAccountExist(signUpUser.getAccountName())) {
      return false;
    }

    String hashedPassword = BCrypt.hashpw(
        signUpUser.getPassword(),
        BCrypt.gensalt()
    );

    User newUser = new Bidder();
    newUser.setAccountName(signUpUser.getAccountName());
    newUser.setPassword(hashedPassword);
    Connection conn = null;

    try {
      conn = DatabaseConnection.getConnection();
      conn.setAutoCommit(false);

      // 1. tạo user
      boolean userCreated = userRepo.createUser(
          conn,
          newUser
      );

      if (!userCreated) {
        conn.rollback();
        return false;
      }
      Wallet wallet = new Wallet(newUser.getId());
      // 2. tạo wallet
      boolean walletCreated = walletRepo.createWallet(
          conn,
          wallet
      );

      if (!walletCreated) {
        conn.rollback();
        return false;
      }

      conn.commit(); // ✅ OK
      return true;

    } catch (Exception e) {
      e.printStackTrace();

      try {
        if (conn != null) conn.rollback();
      } catch (Exception ex) {
        ex.printStackTrace();
      }

    } finally {
      try {
        if (conn != null) conn.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return false;
  }
}