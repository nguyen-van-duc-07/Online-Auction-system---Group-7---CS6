package com.auction.shared.response;

import java.time.LocalDateTime;

public class AuctionExtendedDTO implements ResponseDTO {
  private String auctionId;
  private LocalDateTime newEndTime;

  public AuctionExtendedDTO(String auctionId, LocalDateTime newEndTime) {
    this.auctionId   = auctionId;
    this.newEndTime  = newEndTime;
  }

  public String getAuctionId()          { return auctionId; }
  public LocalDateTime getNewEndTime()  { return newEndTime; }
}