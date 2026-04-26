package com.auction.shared.model.auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionResponseDTO {
    private String id;
    private String itemName;
    private BigDecimal currentHighestPrice;
    private LocalDateTime endTime;
    private String status;
    // Lọc bớt các thông tin nhạy cảm/ ko cần thiết để trả về máy khách
    // Không chứa bidHistory hoặc chỉ chứa 5 cái gần nhất để nhẹ băng thông
}
