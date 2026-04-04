package com.example.auctionserver.model;

import java.math.BigDecimal;

public class PrizedTransaction extends Transaction{
    private BigDecimal highestPrice;

    public PrizedTransaction(String fromId, String receiveId, BigDecimal highestPrice) {
        super(fromId, receiveId);
        this.highestPrice = highestPrice;
    }
}
