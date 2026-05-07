package com.auction.shared.model.user;

import com.auction.shared.model.core.Entity;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@NoArgsConstructor
public class Wallet extends Entity {
  private String bidderId;
  private BigDecimal balance;
  private BigDecimal frozenBalance;


  public Wallet(String bidderId) {
    this.bidderId = bidderId;
    this.balance = BigDecimal.ZERO;
    this.frozenBalance = BigDecimal.ZERO;
  }

  public Wallet(String id, LocalDateTime createdAt, String bidderId, BigDecimal balance, BigDecimal frozenBalance) {
    super(id, createdAt);
    this.bidderId = bidderId;
    this.balance = balance;
    this.frozenBalance = frozenBalance;
  }

  public void deposit(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0");
    }
    this.balance = this.balance.add(amount);
  }

  public BigDecimal getAvailableBalance() {
    return this.balance.subtract(this.frozenBalance);
  }

  public void withdraw(BigDecimal amount) {
    if (getAvailableBalance().compareTo(amount) < 0) {
      throw new IllegalStateException("Số dư khả dụng không đủ để rút!");
    }
    this.balance = this.balance.subtract(amount);
  }

  public void freeze(BigDecimal amount) {
    if (getAvailableBalance().compareTo(amount) < 0) {
      throw new IllegalStateException("Số dư khả dụng không đủ để đặt cược!");
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
    this.frozenBalance = this.frozenBalance.subtract(amount);
    this.balance = this.balance.subtract(amount);
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public BigDecimal getFrozenBalance() {
    return frozenBalance;
  }

  public void setFrozenBalance(BigDecimal frozenBalance) {
    this.frozenBalance = frozenBalance;
  }

  public String getBidderId() {
    return bidderId;
  }

  public void setBidderId(String bidder_id) {
    this.bidderId = bidder_id;
  }
}
