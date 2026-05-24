package com.auction.shared.response;

public class CreateAdminResponseDTO implements ResponseDTO{
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
  public CreateAdminResponseDTO(boolean success, String message) {
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
