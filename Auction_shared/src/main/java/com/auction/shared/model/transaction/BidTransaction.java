package com.auction.shared.model.transaction;

import com.auction.shared.model.core.Entity;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class BidTransaction extends Entity {
  private String auctionId;
  private String bidderId;
  private BigDecimal bidAmount;

  public BidTransaction(String auctionId, String bidderId, BigDecimal bidAmount) {
    if (auctionId == null || auctionId.trim().isEmpty()) {
      throw new IllegalArgumentException("Auction ID không được để trống.");
    }
    if (bidderId == null || bidderId.trim().isEmpty()) {
      throw new IllegalArgumentException("Bidder ID không được để trống.");
    }
    if (bidAmount == null || bidAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Số tiền bid phải lớn hơn 0.");
    }

    this.auctionId = auctionId;
    this.bidderId = bidderId;
    this.bidAmount = bidAmount;
  }
}