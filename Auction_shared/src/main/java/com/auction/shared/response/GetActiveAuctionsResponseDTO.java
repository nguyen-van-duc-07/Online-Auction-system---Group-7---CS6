package com.auction.shared.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Lớp DTO mang gói dữ liệu trả về từ Server cho Client sau khi xử lý yêu cầu
 * lấy danh sách các phiên đấu giá đang hoạt động.
 *
 * <p>Bao gồm trạng thái thành công/thất bại của truy vấn, thông báo từ hệ thống
 * và một danh sách các đối tượng {@link AuctionResponseDTO} đã được đóng gói sẵn.</p>
 */
@Getter
@AllArgsConstructor
public class GetActiveAuctionsResponseDTO implements ResponseDTO {
  /** Cờ đánh dấu trạng thái truy vấn thành công hay thất bại. */
  private boolean success;

  /** Thông báo chi tiết từ Server (ví dụ: "Tải danh sách thành công"). */
  private String message;

  /** Danh sách các phiên đấu giá đang mở đã được chắt lọc dữ liệu. */
  private List<AuctionResponseDTO> activeAuctions;
}
