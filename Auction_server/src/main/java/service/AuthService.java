package service;

import com.auction.shared.enums.NotificationType;
import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.Admin;
import com.auction.shared.request.LoginRequestDTO;
import com.auction.shared.request.SignUpRequestDTO;
import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.User;
import com.auction.shared.model.user.Wallet;
import com.auction.shared.request.UpdateProfileRequestDTO;
import com.auction.shared.util.NotificationTemplate;
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
   * Xử lý đăng nhập của người dùng bằng tài khoản và mật khẩu.
   *
   * <p>Tìm mật khẩu đã mã hóa (hash) trong database và so sánh bằng thuật toán BCrypt.</p>
   *
   * @param loginUser đối tượng {@link LoginRequestDTO} chứa thông tin đăng nhập
   * @return Đối tượng {@link User} nếu đăng nhập thành công, {@code null} nếu sai thông tin
   */
  public static User login(LoginRequestDTO loginUser) {
    String hashedPassword = userRepo.getPasswordByPhoneNumber(loginUser.getPhoneNumber());

    if (hashedPassword == null) {
      return null;
    }

    // Kiểm tra mật khẩu và mật khẩu đã mã hoá bằng thư viện BCrypt xem có giống nhau không
    if (BCrypt.checkpw(loginUser.getPassword(), hashedPassword)) {
      User user = userRepo.getUserByPhoneNumberNameOrId(loginUser.getPhoneNumber(), null);
      if (user.getRole() == UserRole.BIDDER) {
        Bidder bidder = (Bidder) user;
        bidder.setWallet(walletRepo.getWalletByUserId(bidder.getId()));
        return bidder;
      } else {
        return  (Admin) user;
      }
    }
    return null; // Sai mật khẩu
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

    if (userRepo.isAccountExist(signUpUser.getPhoneNumber())) {
      return false;
    }

    String hashedPassword = BCrypt.hashpw(
        signUpUser.getPassword(),
        BCrypt.gensalt()
    );

    User newUser = new Bidder();
    newUser.setAccountName(newUser.getDefaultAccountName());
    newUser.setPhoneNumber(signUpUser.getPhoneNumber());
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

      conn.commit(); // OK
      NotificationService notifService = new NotificationService();
      notifService.sendFromNotification(NotificationTemplate.welcome(newUser.getId()));
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

  public static User updateProfile(UpdateProfileRequestDTO updateProfileReq) {
    User user = new Bidder();
    user.setId(updateProfileReq.getUserId());
    user.setAccountName(updateProfileReq.getAccountName());
    user.setEmail(updateProfileReq.getEmail());
    user.setAddress(updateProfileReq.getAddress());
    user.setDob(updateProfileReq.getBirthDate());

    boolean isSuccess = userRepo.updateProfile(user);
    if (isSuccess) {
      return userRepo.getUserByPhoneNumberNameOrId(null, user.getId());
    }
    return null;
  }
}