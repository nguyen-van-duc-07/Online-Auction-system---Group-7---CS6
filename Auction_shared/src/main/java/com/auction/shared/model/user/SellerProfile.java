package com.auction.shared.model.user;

import com.auction.shared.model.core.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class SellerProfile extends Entity {
  private String userId;
  private String brandName;
  private String citizenIdentityCard;
  private String location;
  private String bankAccount;
  private String bankName;
  private float rating;
  private final List<String> myAuctionIds = new ArrayList<>();

  public SellerProfile(String id, LocalDateTime createdAt) {
    super(id, createdAt);
  }

  public SellerProfile(String userId, String brandName, String citizenIdentityCard, String location, String bankAccount, String bankName) {
    this.userId = userId;
    this.brandName = brandName;
    this.citizenIdentityCard = citizenIdentityCard;
    this.location = location;
    this.bankAccount = bankAccount;
    this.bankName = bankName;
    this.rating = 5;
  }

  public SellerProfile(String userId) {
    super();
    this.userId = userId;
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