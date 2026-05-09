package com.auction.shared.response;

import java.math.BigDecimal;

public class NewBidDTO implements ResponseDTO{
  private String auctionId;
  private String bidderId;
  private BigDecimal bidAmount;

  public NewBidDTO(String auctionId, String bidderId, BigDecimal bidAmount) {
    this.auctionId = auctionId;
    this.bidderId = bidderId;
    this.bidAmount = bidAmount;
  }

  public String getAuctionId() {
    return auctionId;
  }

  public void setAuctionId(String auctionId) {
    this.auctionId = auctionId;
  }

  public String getBidderId() {
    return bidderId;
  }

  public void setBidderId(String bidderId) {
    this.bidderId = bidderId;
  }

  public BigDecimal getBidAmount() {
    return bidAmount;
  }

  public void setBidAmount(BigDecimal bidAmount) {
    this.bidAmount = bidAmount;
  }
}
