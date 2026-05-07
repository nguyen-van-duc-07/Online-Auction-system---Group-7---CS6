package com.auction.shared.model.transaction;

import com.auction.shared.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter

public class WalletTransaction extends Transaction {
    private String fromId, receivedId;
    private BigDecimal amount;
    private TransactionType type;
    public WalletTransaction(String fromId, String receivedId, BigDecimal amount, TransactionType type){
        super(fromId, receivedId);
        this.amount = amount;
        this.type = type;
    }
    @Override
    public void showInfo() {
        System.out.println("Giao dịch ví: " + type + " số tiền: " + amount);
    }
}
