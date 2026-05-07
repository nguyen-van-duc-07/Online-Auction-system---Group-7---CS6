package com.auction.shared.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lớp DTO (Data Transfer Object) đại diện cho dữ liệu của một phiên đấu giá
 * được gửi từ Server về Client để hiển thị trên giao diện trang chủ (Feed).
 *
 * <p>Lớp này đã được lược bỏ các thông tin nhạy cảm và phức tạp (như lịch sử đặt giá,
 * thông tin người bán, khóa lạc quan) để tối ưu băng thông mạng và đảm bảo bảo mật
 * khi truyền tải qua Socket.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionResponseDTO implements ResponseDTO {
  /**
   * Mã định danh duy nhất của phiên đấu giá.
   */
  private String id;

  /**
   * Tên của sản phẩm đang được đấu giá.
   */
  private String itemName;

  /**
   * Mức giá cao nhất hiện tại của phiên đấu giá.
   */
  private BigDecimal currentHighestPrice;

  /**
   * Thời điểm kết thúc phiên đấu giá.
   */
  private LocalDateTime endTime;

  /**
   * Trạng thái hiện tại của phiên đấu giá (ví dụ: WAITING, ACTIVE, FINISHED).
   */
  private String status;

  /**
   * Đoạn mô tả ngắn gọn về sản phẩm để hiển thị trên thẻ (card) sản phẩm.
   */
  private String itemDescription;
  // Lọc bớt các thông tin nhạy cảm/ ko cần thiết để trả về máy khách
  // Không chứa bidHistory hoặc chỉ chứa 5 cái gần nhất để nhẹ băng thông
}
