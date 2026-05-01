package com.auction.shared.model.user;

import lombok.Getter;

import java.math.BigDecimal;
@Getter

public class Wallet {
    private BigDecimal balance;
    private BigDecimal frozenBalance;

    // Thêm version để dùng Optimistic Locking ở tầng DB
    private Long version;

    public Wallet() {
        this.balance = BigDecimal.ZERO;
        this.frozenBalance = BigDecimal.ZERO;
    }

    public void deposit(BigDecimal amount){
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0");
        }
        this.balance = this.balance.add(amount);
    }

    public BigDecimal getAvailableBalance() {
        return this.balance.subtract(this.frozenBalance);
    }

    public void withdraw(BigDecimal amount){
        if (getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Số dư khả dụng không đủ để rút!");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void freeze(BigDecimal amount) {
        if (getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Số dư khả dụng không đủ để đóng băng!");
        }
        this.frozenBalance = this.frozenBalance.add(amount);
    }

    public void unfreeze(BigDecimal amount) {
        if (this.frozenBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Lỗi hệ thống: Tiền đóng băng không đủ để mở!");
        }
        this.frozenBalance = this.frozenBalance.subtract(amount);
    }

    public void payWinningBid(BigDecimal amount) {
        if (this.frozenBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Lỗi hệ thống: Tiền đóng băng không đủ để thanh toán!");
        }
        // Khi thanh toán từ tiền đã đóng băng:
        // Trừ cả ở balance (tổng) và frozenBalance (tạm giữ)
        this.frozenBalance = this.frozenBalance.subtract(amount);
        this.balance = this.balance.subtract(amount);
    }
}