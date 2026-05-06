package com.auction.shared.model.transaction;

import lombok.Getter;

import java.math.BigDecimal;
@Getter

public class BidTransaction extends Transaction {
  private String auctionId;
  private BigDecimal bidAmount;

  public BidTransaction(String bidderId, String auctionId, BigDecimal bidAmount) {
    // Gán bidderId vào fromId, auctionId vào receiveId của lớp cha
    super(bidderId, auctionId);
    this.bidAmount = bidAmount;
  }

  @Override
  public void showInfo() {
    // Gọi getFromId() và getReceiveId() từ lớp cha
    System.out.println("[BID] User " + getFromId() +
            " đặt giá " + bidAmount +
            " cho Auction " + getReceiveId());
  }
}