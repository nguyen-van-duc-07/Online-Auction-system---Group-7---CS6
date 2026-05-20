package com.auction.shared.response;

import com.auction.shared.model.user.UserDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * Lớp DTO mang theo kết quả của quá trình Đăng nhập từ Server trả về Client.
 *
 * <p>
 * Lớp này triển khai {@link ResponseDTO} để Client có thể tự động nhận diện
 * thông qua Switch Pattern Matching. Dữ liệu mang theo bao gồm trạng thái
 * thành công/thất bại và câu thông báo chi tiết để hiển thị lên UI.
 * </p>
 *
 * @see com.auction.shared.request.LoginRequestDTO
 */
@Getter
@Setter
public class LoginResponseDTO implements ResponseDTO {
  /**
   * Trạng thái xác thực đăng nhập.
   * {@code true} nếu tài khoản và mật khẩu hợp lệ, {@code false} nếu sai thông tin hoặc lỗi.
   */
  private boolean success;

  /**
   * Thông báo chi tiết từ Server (Ví dụ: "Đăng nhập thành công!", "Sai mật khẩu!").
   * Chuỗi này được thiết kế để Client có thể in thẳng ra các hộp thoại (Alert) trên JavaFX.
   */
  private String message;

  /**
   * Đối tượng chứa thông tin cơ bản của người dùng vừa đăng nhập thành công.
   * Sẽ mang giá trị {@code null} nếu đăng nhập thất bại.
   */
  private UserDTO user;

  /**
   * Khởi tạo một đối tượng phản hồi đăng nhập.
   *
   * @param success trạng thái đăng nhập
   * @param message thông báo trả về cho người dùng
   * @param user thông tin người dùng (truyền null nếu đăng nhập lỗi)
   */
  public LoginResponseDTO(boolean success, String message, UserDTO user) {
    this.success = success;
    this.message = message;
    this.user = user;
  }
}
