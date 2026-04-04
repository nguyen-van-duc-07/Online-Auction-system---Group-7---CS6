package com.example.auctionserver.model;

public class AutoBidConfig extends Entity{
    private String bidderId;
    private String auctionId;
    private double startPrice;
    private double maxBid;
    private double increment;

    public AutoBidConfig(String bidderId, String auctionId, double startPrice, double maxBid, double increment) {
        this.bidderId = bidderId;
        this.auctionId = auctionId;
        this.startPrice = startPrice;
        this.maxBid = maxBid;
        this.increment = increment;
    }
}
