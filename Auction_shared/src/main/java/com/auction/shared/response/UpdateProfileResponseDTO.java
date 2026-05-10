package com.auction.shared.response;

import com.auction.shared.model.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Lớp DTO mang kết quả xử lý yêu cầu cập nhật thông tin tài khoản từ Server trả về Client.
 *
 * <p>Báo cho Client biết việc chèn dữ liệu vào bảng users đã thành công hay thất bại.</p>
 */
@Getter
@AllArgsConstructor
public class UpdateProfileResponseDTO implements ResponseDTO {
  /**
   * Trạng thái cập nhật thông tin tài khoản từ Server.
   */
  private boolean isSuccess;
  /**
   * Câu thông báo từ Server phản hồi yêu cầu cập nhật thông tin tài khoản.
   */
  private String message;
  /**
   * Thông tin người dùng sau khi cập nhật.
   */
  private UserDTO userAfterUpdatingProfile;
}
