package com.auction.shared.response;

import com.auction.shared.enums.AuctionStatus;

public class AuctionStatusUpdateDTO implements ResponseDTO {
  private String id;
  private AuctionStatus auctionStatus;

  public AuctionStatusUpdateDTO(String id, AuctionStatus auctionStatus) {
    this.id = id;
    this.auctionStatus = auctionStatus;
  }

  public String getId() {
    return id;
  }

  public AuctionStatus getAuctionStatus() {
    return auctionStatus;
  }
}
