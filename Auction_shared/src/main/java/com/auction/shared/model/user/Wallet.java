package com.auction.shared.model.user;

import com.auction.shared.model.core.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
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

  public void withdraw(BigDecimal amount) {
    if (this.balance.compareTo(amount) < 0) {
      throw new IllegalStateException("Số dư khả dụng không đủ để rút!");
    }
    this.balance = this.balance.subtract(amount);
  }

  public void freeze(BigDecimal amount) {
    if (this.balance.compareTo(amount) < 0) {
      throw new IllegalStateException("Số dư khả dụng không đủ để đặt cược!");
    }
    this.frozenBalance = this.frozenBalance.add(amount);
    this.balance = this.balance.subtract(amount);
  }

  public void unfreeze(BigDecimal amount) {
    if (this.frozenBalance.compareTo(amount) < 0) {
      throw new IllegalStateException("Lỗi hệ thống: Tiền đóng băng không đủ để mở!");
    }
    this.frozenBalance = this.frozenBalance.subtract(amount);
    this.balance = this.balance.add(amount);
  }

  public void payWinningAuction(BigDecimal depositAmount, BigDecimal remainingAmount) {
    if (this.frozenBalance.compareTo(depositAmount) < 0) {
      throw new IllegalStateException("Tiền cọc đóng băng không đủ!");
    }

    if (this.balance.compareTo(remainingAmount) < 0) {
      throw new IllegalStateException("Tiền thanh toán còn lại không đủ!");
    }

    this.frozenBalance = this.frozenBalance.subtract(depositAmount);
    this.balance = this.balance.subtract(remainingAmount);
  }
  public void penaltyDeposit(BigDecimal depositAmount) {
    if (this.frozenBalance.compareTo(depositAmount) < 0) {
      throw new IllegalStateException("Tiền cọc đóng băng không đủ!");
    }
    this.frozenBalance = this.frozenBalance.subtract(depositAmount);
  }
}
