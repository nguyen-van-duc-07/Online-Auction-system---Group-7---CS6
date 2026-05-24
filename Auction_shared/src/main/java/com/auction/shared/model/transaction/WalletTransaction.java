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
    private String referenceId;
    private WalletTransactionStatus status;

    @Builder
    public WalletTransaction(String walletId, WalletTransactionType type, BigDecimal amount, String referenceId, WalletTransactionStatus status) {
        Objects.requireNonNull(walletId, "Mã ví (Wallet ID) không được để trống!");
        Objects.requireNonNull(type, "Loại giao dịch không được để trống!");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền giao dịch phải lớn hơn 0!");
        }

        this.walletId = walletId;
        this.type = type;
        this.amount = amount;
        this.referenceId = referenceId;
        this.status = status;
    }
}