package com.auction.shared.model.transaction;

public abstract class Transaction {
  String fromId;
  String receiveId;

  public Transaction(String fromId, String receiveId) {
    this.fromId = fromId;
    this.receiveId = receiveId;
  }
}
