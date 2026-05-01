package com.auction.shared.model.user;

import com.auction.shared.model.core.Entity;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
@Getter
public class SellerProfile extends Entity {
    private String managerId;
    private float rating;
    private final List<String> myAuctionIds;

    public SellerProfile(String managerId) {
        super();
        this.managerId = managerId;
        this.rating = 5;
        this.myAuctionIds = new ArrayList<>();
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
