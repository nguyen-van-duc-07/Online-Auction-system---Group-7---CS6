package com.auction.shared.request;

import lombok.Getter;

@Getter
public class CancelAutoBidRequestDTO implements RequestDTO {
  private String userId;
  private String auctionId;

  public CancelAutoBidRequestDTO(String userId, String auctionId) {
    this.userId    = userId;
    this.auctionId = auctionId;
  }

}