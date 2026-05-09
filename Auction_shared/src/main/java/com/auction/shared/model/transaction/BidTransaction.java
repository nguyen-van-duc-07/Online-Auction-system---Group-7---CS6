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
    this.auctionId = auctionId;
    this.bidderId = bidderId;
    this.bidAmount = bidAmount;
  }

}


