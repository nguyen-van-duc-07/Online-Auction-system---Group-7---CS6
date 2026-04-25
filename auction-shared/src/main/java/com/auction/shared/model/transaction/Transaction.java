package com.auction.shared.model.transaction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Transaction {
    private String fromId;
    private String receiveId;

    public Transaction(String fromId, String receiveId) {
        this.fromId = fromId;
        this.receiveId = receiveId;
    }
    abstract public void showInfo();
}
