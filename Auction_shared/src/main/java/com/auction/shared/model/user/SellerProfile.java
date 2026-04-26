package com.auction.shared.model.user;

import com.auction.shared.model.core.Entity;

import java.util.ArrayList;
import java.util.List;

public class SellerProfile extends Entity {
    private String managerId;
    private float rating;
    List<String> myAuctionIds;

    public SellerProfile(String managerId) {
        super();
        this.managerId = managerId;
        this.rating = 5;
        this.myAuctionIds = new ArrayList<>();
    }
    public void addAuction (String AuctionId){
        myAuctionIds.add(AuctionId);
    }
}
