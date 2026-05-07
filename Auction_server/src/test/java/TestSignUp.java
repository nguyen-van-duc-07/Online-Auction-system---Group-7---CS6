import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.User;
import com.auction.shared.request.LoginRequestDTO;
import com.auction.shared.request.SignUpRequestDTO;
import repository.UserRepository;
import repository.WalletRepository;
import service.AuthService;

public class TestSignUp {
  public static void main(String[] args) {
    System.out.println("Test thu dang ki tai khoan da ton tai");
    SignUpRequestDTO req = new SignUpRequestDTO("2", "2");
    boolean isNotExits = AuthService.signUp(req);
    if (!isNotExits) {
      System.out.println(">>>TÀI KHOẢN ĐÃ TỒN TẠI");
    } else {
      System.out.println(">>>TEST FAIL");
    }
    System.out.println();

    System.out.println("Test thu dang ki tai khoan hop le");
    req = new SignUpRequestDTO("7", "7");
    isNotExits = AuthService.signUp(req);
    if (isNotExits) {
      System.out.println(">>>ĐĂNG KÍ THÀNH CÔNG");
    } else {
      System.out.println(">>>TEST FAIL");
    }
    System.out.println();

    System.out.println("Thực hiện đăng nhập");
    LoginRequestDTO loginReq = new LoginRequestDTO("7", "7");
    User loginUser = AuthService.login(loginReq);
    if (loginUser != null) {
      System.out.println(">>>ĐĂNG NHẬP THÀNH CÔNG");
    } else {
      System.out.println(">>>TEST FAIL");
    }
    System.out.println();

    System.out.println("Thực hiện xóa tài khoản");
    if (loginUser.getRole() == UserRole.BIDDER) {
      Bidder bidder = (Bidder) loginUser;
      if (UserRepository.deleteAccount(bidder)) {
        System.out.println(">>>XÓA TÀI KHOẢN THÀNH CÔNG");
      } else {
        System.out.println(">>>XÓA TÀI KHOẢN THẤT BẠI");
      }
    }
    System.out.println();
    System.out.println("Kiểm tra lại tài khoản còn tồn tại");
    if (!UserRepository.isAccountExist("7")){
      System.out.println(">>>TÀI KHOẢN KO CÒN TỒN TẠI");
    } else {
      System.out.println(">>>TEST FAIL");
    }
  }
}
