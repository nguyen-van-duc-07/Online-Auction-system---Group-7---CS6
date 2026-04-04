package com.example.auctionserver.model;

public abstract class Transaction {
    String fromId;
    String receiveId;

    public Transaction(String fromId, String receiveId) {
        this.fromId = fromId;
        this.receiveId = receiveId;
    }
}
