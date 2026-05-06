package servercontroller;

import com.auction.shared.model.user.User;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.LoginRequestDTO;
import com.auction.shared.request.SignUpRequestDTO;
import com.auction.shared.response.LoginResponseDTO;
import com.auction.shared.response.SignUpResponseDTO;
import service.AuthService;

/**
 * Bộ điều hướng trung tâm (Controller) xử lý logic phân nhánh cho các yêu cầu từ Client.
 * <p>
 * Lớp này đóng vai trò cầu nối thiết yếu giữa tầng mạng (Network) và tầng dịch vụ (Service).
 * Nhiệm vụ của nó là nhận các đối tượng Request cụ thể, kích hoạt các phương thức nghiệp vụ
 * trong tầng Service (như {@code AuthService}), và đóng gói kết quả (trạng thái, thông báo)
 * vào các đối tượng {@code ResponseDTO} để trả về cho Client.
 * </p>
 *
 * @see service.AuthService
 * @see com.auction.shared.response.ResponseDTO
 */
public class RequestHandler {
  public static LoginResponseDTO login(LoginRequestDTO loginReq) {
    User loggedInUser = AuthService.login(loginReq);

    if (loggedInUser != null) {
      UserDTO userDTO = UserDTO.builder()
              .id(loggedInUser.getId())
              .accountName(loggedInUser.getAccountName())
              .email(loggedInUser.getEmail())
              .phoneNumber(loggedInUser.getPhoneNumber())
              .dob(loggedInUser.getDob())
              .build();

      return new LoginResponseDTO(true, "Đăng nhập thành công!", userDTO);
    } else {
      return new LoginResponseDTO(false, "Sai tài khoản hoặc mật khẩu", null);
    }
  }

  public static SignUpResponseDTO signup(SignUpRequestDTO signUpReq) {
    boolean isSuccess = AuthService.signUp(signUpReq);
    String msg = isSuccess ? "Đăng ký tài khoản thành công!" :
        "Tài khoản đã tồn tại hoặc lỗi hệ thống!";
    return new SignUpResponseDTO(isSuccess, msg);
  }
}
