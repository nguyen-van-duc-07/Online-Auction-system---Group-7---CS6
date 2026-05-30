package com.auction.shared.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuctionExtendedDTO implements ResponseDTO {
  private String auctionId;
  private LocalDateTime newEndTime;

  public AuctionExtendedDTO(String auctionId, LocalDateTime newEndTime) {
    this.auctionId   = auctionId;
    this.newEndTime  = newEndTime;
  }
}