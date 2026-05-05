package servercontroller;

import com.auction.shared.request.LoginRequestDTO;
import com.auction.shared.request.SignUpRequestDTO;
import service.AuthService;

/**
 * Lớp điều hướng và xử lý các yêu cầu (requests) từ client gửi đến server.
 * Đóng vai trò như một Controller, tiếp nhận dữ liệu request, phân phối đến các
 * Service nghiệp vụ tương ứng (Xác thực, Đấu giá, Quản lý người dùng, v.v.)
 * và trả về kết quả phản hồi (response) cho client.
 */
public class RequestHandler {
  public static String login(LoginRequestDTO loginUser) {
    boolean isSuccess = AuthService.login(loginUser);
    if (isSuccess) {
      return "LOGIN_SUCCESS";
    }
    return "LOGIN_FAILED";
  }

  /**
   * Xử lý yêu cầu đăng ký tài khoản mới của người dùng.
   *
   * @param signUpUser đối tượng {@link SignUpRequestDTO} chứa các thông tin cần thiết để đăng ký
   * @return chuỗi "SIGNUP_SUCCESS" nếu đăng ký thành công, hoặc "SIGNUP_FAILED" nếu thất bại
   */
  public static String signup(SignUpRequestDTO signUpUser) {
    boolean isSuccess = AuthService.signUp(signUpUser);

    if (isSuccess) return "SIGNUP_SUCCESS";
    return "SIGNUP_FAILED";
  }
}
