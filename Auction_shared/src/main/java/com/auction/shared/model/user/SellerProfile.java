package com.auction.shared.model.user;

import com.auction.shared.model.core.Entity;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
public class SellerProfile extends Entity {
  private String managerId;
  private float rating;
  private final List<String> myAuctionIds = new ArrayList<>();

  public SellerProfile(String id, LocalDateTime createdAt) {
    super(id, createdAt);
  }

  public SellerProfile(String managerId) {
    super();
    this.managerId = managerId;
    this.rating = 5;
  }
  public void addAuction (String auctionId){
    if (auctionId == null || auctionId.isEmpty()) {
      throw new IllegalArgumentException("Auction ID không hợp lệ");
    }
    this.myAuctionIds.add(auctionId);
  }
  public void updateRating(float newRating) {
    // Giả sử logic đơn giản: (Rating cũ + Rating mới) / 2
    this.rating = (this.rating + newRating) / 2;
  }
}