package service;

import com.auction.shared.model.user.User;
import org.mindrot.jbcrypt.BCrypt;
import repository.UserRepository;

public class AuthService {

  private static UserRepository repo = new UserRepository();

  public static boolean login(String accountName, String password) {
    String hashedPassword = repo.getPasswordByAccountName(accountName);

    if (hashedPassword == null) return false;

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