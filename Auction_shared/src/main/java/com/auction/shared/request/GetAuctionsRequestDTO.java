package com.auction.shared.request;

import com.auction.shared.enums.AuctionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Lớp DTO mang yêu cầu lấy danh sách các phiên đấu giá đang mở từ Client gửi lên Server.
 *
 * <p>Lớp này implements {@link RequestDTO} để được Server tự động phân luồng.
 * Do yêu cầu chỉ đơn giản là "lấy tất cả danh sách đang mở", lớp này không cần
 * chứa thêm bất kỳ trường dữ liệu (parameter) nào bên trong.</p>
 */
@Getter
@NoArgsConstructor
public class GetAuctionsRequestDTO implements RequestDTO {
  private AuctionStatus status;

  public GetAuctionsRequestDTO(AuctionStatus status) {
    this.status = status;
  }
}
