package com.auction.shared.response;

import com.auction.shared.enums.AuctionStatus;

public class AuctionStatusUpdateDTO implements ResponseDTO {
  private String id;
  private AuctionStatus auctionStatus;
  private boolean closedByAdmin;

  public AuctionStatusUpdateDTO(String id, AuctionStatus auctionStatus) {
    this(id, auctionStatus, false);
  }

  public AuctionStatusUpdateDTO(String id, AuctionStatus auctionStatus, boolean closedByAdmin) {
    this.id = id;
    this.auctionStatus = auctionStatus;
    this.closedByAdmin = closedByAdmin;
  }

  public String getId() {
    return id;
  }

  public AuctionStatus getAuctionStatus() {
    return auctionStatus;
  }

  public boolean isClosedByAdmin() {
    return closedByAdmin;
  }
}
