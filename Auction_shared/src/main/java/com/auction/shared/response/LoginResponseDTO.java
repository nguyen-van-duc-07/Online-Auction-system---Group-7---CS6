package com.auction.shared.response;

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
   * Khởi tạo một đối tượng phản hồi đăng nhập.
   *
   * @param success trạng thái đăng nhập
   * @param message thông báo trả về cho người dùng
   */
  public LoginResponseDTO(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }
}
