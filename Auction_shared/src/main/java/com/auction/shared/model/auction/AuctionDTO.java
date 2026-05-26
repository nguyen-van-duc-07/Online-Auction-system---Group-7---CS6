package com.auction.shared.model.auction;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.enums.ItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
/**
 * Class chứa những thông tin cơ bản về một phiên đấu giá
 */
public class AuctionDTO implements Serializable {
    private String auctionId;
    private String itemName;
    private ItemType itemType;
    private BigDecimal currentPrice;
    private AuctionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
