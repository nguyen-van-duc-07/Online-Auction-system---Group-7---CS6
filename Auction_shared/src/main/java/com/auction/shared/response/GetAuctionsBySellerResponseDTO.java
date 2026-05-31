package com.auction.shared.response;

import com.auction.shared.model.auction.AuctionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Lớp DTO mang gói dữ liệu trả về từ Server cho Seller sau khi xử lý yêu cầu
 * lấy danh sách các phiên đấu giá mà Seller đăng bán.
 *
 * <p>Bao gồm trạng thái thành công/thất bại của truy vấn, thông báo từ hệ thống
 * và một danh sách các đối tượng {@link AuctionDTO} đã được đóng gói sẵn.</p>
 */
@Getter
@AllArgsConstructor
public class GetAuctionsBySellerResponseDTO implements ResponseDTO {
  /** Cờ đánh dấu trạng thái truy vấn thành công hay thất bại. */
  private boolean success;

  /** Thông báo chi tiết từ Server (ví dụ: "Tải danh sách thành công"). */
  private String message;

  /** Danh sách các phiên đấu giá đang mở đã được chắt lọc dữ liệu. */
  private List<AuctionDTO> auctions;
}
