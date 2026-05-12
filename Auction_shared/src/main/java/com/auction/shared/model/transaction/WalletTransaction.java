package com.auction.shared.model.transaction;

import com.auction.shared.enums.WalletTransactionStatus;
import com.auction.shared.enums.WalletTransactionType;
import com.auction.shared.model.core.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter

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

    public WalletTransaction(String walletId, WalletTransactionType type, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, BigDecimal frozenBefore, BigDecimal frozenAfter, String referenceId, WalletTransactionStatus status) {
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
