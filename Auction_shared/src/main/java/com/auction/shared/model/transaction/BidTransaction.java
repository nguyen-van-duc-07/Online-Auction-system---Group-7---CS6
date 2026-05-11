package com.auction.shared.model.transaction;

import com.auction.shared.model.core.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor

public class BidTransaction extends Entity {
  private String auctionId;
  private String bidderId;
  private BigDecimal bidAmount;
}


