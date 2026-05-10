package com.auction.shared.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@NoArgsConstructor
@Getter
@Setter
public class PlaceBidRequestDTO implements RequestDTO {
  private String auctionId;
  private String bidderId;
  private BigDecimal bidAmount;

  public PlaceBidRequestDTO(String auctionId, String bidderId, BigDecimal bidAmount) {
    this.auctionId = auctionId;
    this.bidderId = bidderId;
    this.bidAmount = bidAmount;
  }
}
