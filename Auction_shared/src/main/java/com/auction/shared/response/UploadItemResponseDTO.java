package com.auction.shared.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Lớp DTO mang kết quả xử lý yêu cầu Đăng bán sản phẩm từ Server trả về Client.
 *
 * <p>Báo cho Client biết việc chèn dữ liệu vào bảng items đã thành công hay thất bại
 * để thực hiện chuyển hướng màn hình.</p>
 */
@Getter
@AllArgsConstructor
public class UploadItemResponseDTO implements ResponseDTO {

  /** Trạng thái lưu sản phẩm (true nếu chèn DB thành công, ngược lại là false). */
  private boolean success;

  /** Câu thông báo chi tiết từ Server để hiển thị lên hộp thoại giao diện. */
  private String message;
}
