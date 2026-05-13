package com.auction.shared.response;

import java.io.Serializable;
import java.math.BigDecimal;

public class AuctionResultDTO implements ResponseDTO{
  private String auctionId;
  private String winnerId;
  private BigDecimal finalPrice;

  public AuctionResultDTO(String auctionId, String winnerId, BigDecimal finalPrice) {
    this.auctionId  = auctionId;
    this.winnerId   = winnerId;
    this.finalPrice = finalPrice;
  }

  public String getAuctionId()      { return auctionId; }
  public String getWinnerId()       { return winnerId; }
  public BigDecimal getFinalPrice() { return finalPrice; }
}