package com.auction.shared.model.transaction;

import com.auction.shared.enums.WalletTransactionStatus;
import com.auction.shared.enums.WalletTransactionType;
import com.auction.shared.model.core.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@NoArgsConstructor
@Getter

public class WalletTransaction extends Entity {
    private String walletId;
    private WalletTransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private BigDecimal frozenBefore;
    private BigDecimal frozenAfter;
    private String referenceId;
    private WalletTransactionStatus status;

    @Builder
    public WalletTransaction(String walletId, WalletTransactionType type, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, BigDecimal frozenBefore, BigDecimal frozenAfter, String referenceId, WalletTransactionStatus status) {
        Objects.requireNonNull(walletId, "WalletId must not be null");
        Objects.requireNonNull(type, "Transaction type must not be null");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }

        this.walletId = walletId;
        this.type = type;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.frozenBefore = frozenBefore;
        this.frozenAfter = frozenAfter;
        this.referenceId = referenceId;
        this.status = status;
    }
}
