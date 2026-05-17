package com.auction.shared.model.auction;

import com.auction.shared.model.core.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AutoBidConfig extends Entity {
  private String userId;
  private String auctionId;
  private BigDecimal maxPrice;
  private BigDecimal stepAmount;
  private boolean active;

  public AutoBidConfig(String userId,
                       String auctionId,
                       BigDecimal maxPrice,
                       BigDecimal stepAmount) {
    this.userId    = userId;
    this.auctionId = auctionId;
    this.maxPrice  = maxPrice;
    this.stepAmount = stepAmount;
    this.active    = true;
  }
}