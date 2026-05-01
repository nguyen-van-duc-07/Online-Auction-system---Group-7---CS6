package com.auction.shared.model.auction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO dùng để gửi yêu cầu đặt giá từ Client lên Server.
 * Chứa thông tin tối thiểu cần thiết để xử lý một lượt bid.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRequestDTO implements Serializable {

    // Khai báo để đảm bảo quá trình truyền đối tượng qua Socket không bị lỗi phiên bản
    private static final long serialVersionUID = 1L;

    private Long auctionId;      // ID phiên đấu giá muốn đặt
    private String bidderId;     // ID của người đặt giá (User ID)
    private BigDecimal bidAmount; // Số tiền muốn đặt

    // Có thể thêm trường này nếu muốn Server biết thời điểm máy khách bấm nút
    // private LocalDateTime clientTimestamp;
}