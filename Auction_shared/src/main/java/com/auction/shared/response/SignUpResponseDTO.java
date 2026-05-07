package com.auction.shared.response;

/**
 * Lớp DTO mang theo kết quả của quá trình Đăng ký tài khoản từ Server trả về Client.
 *
 * <p>
 * Tương tự như các Response khác, lớp này triển khai {@link ResponseDTO}.
 * Khi nhận được gói tin này, Client sẽ kiểm tra trạng thái {@code success}
 * để quyết định việc chuyển hướng người dùng về trang Đăng nhập hay hiển thị cảnh báo lỗi.
 * </p>
 *
 * @see com.auction.shared.request.SignUpRequestDTO
 */
public class SignUpResponseDTO implements ResponseDTO {

  /**
   * Trạng thái đăng ký.
   * {@code true} nếu dữ liệu lưu thành công vào Database,
   * {@code false} nếu tài khoản đã tồn tại hoặc lỗi hệ thống.
   */
  private boolean success;

  /**
   * Thông báo chi tiết từ Server (Ví dụ: "Đăng ký thành công!", "Tài khoản đã tồn tại!").
   */
  private String message;

  /**
   * Khởi tạo một đối tượng phản hồi đăng ký.
   *
   * @param success trạng thái đăng ký
   * @param message thông báo trả về cho người dùng
   */
  public SignUpResponseDTO(boolean success, String message) {
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
