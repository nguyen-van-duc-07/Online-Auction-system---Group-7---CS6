package com.auction.shared.response;

import java.io.Serializable;
import java.math.BigDecimal;

public class AuctionPriceUpdateDTO implements ResponseDTO {
  private String auctionId;
  private BigDecimal newPrice;

  public AuctionPriceUpdateDTO(String auctionId, BigDecimal newPrice) {
    this.auctionId = auctionId;
    this.newPrice  = newPrice;
  }

  public String getAuctionId()      { return auctionId; }
  public BigDecimal getNewPrice()   { return newPrice; }
}