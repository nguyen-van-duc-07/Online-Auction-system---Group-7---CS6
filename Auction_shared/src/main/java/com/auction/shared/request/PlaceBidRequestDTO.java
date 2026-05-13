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
  private String bidderName;
  private BigDecimal bidAmount;

  public PlaceBidRequestDTO(String auctionId, String bidderId, String bidderName, BigDecimal bidAmount) {
    this.auctionId = auctionId;
    this.bidderId = bidderId;
    this.bidderName = bidderName;
    this.bidAmount = bidAmount;
  }
}
