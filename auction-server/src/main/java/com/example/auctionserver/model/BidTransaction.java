package com.example.auctionserver.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidTransaction extends Transaction{
    private BigDecimal amount;

    public BidTransaction(String fromId, String receiveId, BigDecimal amount) {
        super(fromId, receiveId);
        this.amount = amount;
    }
}
