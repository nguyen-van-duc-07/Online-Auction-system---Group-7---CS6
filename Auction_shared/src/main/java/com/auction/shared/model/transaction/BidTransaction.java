package com.auction.shared.model.transaction;

import java.math.BigDecimal;

public class BidTransaction extends Transaction{
    private BigDecimal amount;

    public BidTransaction(String fromId, String receiveId, BigDecimal amount) {
        super(fromId, receiveId);
        this.amount = amount;
    }
}
