package com.auction.shared.model.order;

import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.core.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Order extends Entity {
  private String auctionId;
  private String buyerId;
  private String sellerId;
  private BigDecimal finalPrice;
  private BigDecimal depositAmount;
  private BigDecimal remainingAmount;
  private OrderStatus status;
  private LocalDateTime resolvedAt;
  private String consigneeName;
  private String phoneNumber;
  private String address;
  private String brandName;
  private String location;
  private String itemName;

  public Order(String auctionId, String buyerId, String sellerId, BigDecimal finalPrice, BigDecimal depositAmount, BigDecimal remainingAmount, OrderStatus status, String consigneeName, String brandName, String location, String itemName) {
    this.auctionId = auctionId;
    this.buyerId = buyerId;
    this.sellerId = sellerId;
    this.finalPrice = finalPrice;
    this.depositAmount = depositAmount;
    this.remainingAmount = remainingAmount;
    this.status = status;
    this.consigneeName = consigneeName;
    this.brandName = brandName;
    this.location = location;
    this.itemName = itemName;
  }

  public Order(String auctionId, String buyerId, String sellerId, BigDecimal finalPrice, BigDecimal depositAmount, BigDecimal remainingAmount, OrderStatus status, String consigneeName, String phoneNumber, String address, String brandName, String location, String itemName) {
    this.auctionId = auctionId;
    this.buyerId = buyerId;
    this.sellerId = sellerId;
    this.finalPrice = finalPrice;
    this.depositAmount = depositAmount;
    this.remainingAmount = remainingAmount;
    this.status = status;
    this.consigneeName = consigneeName;
    this.phoneNumber = phoneNumber;
    this.address = address;
    this.brandName = brandName;
    this.location = location;
    this.itemName = itemName;
  }

  public Order(String auctionId, String buyerId, String sellerId, BigDecimal finalPrice, BigDecimal depositAmount, OrderStatus status, LocalDateTime resolvedAt, String consigneeName, String phoneNumber, String address) {
    this.auctionId = auctionId;
    this.buyerId = buyerId;
    this.sellerId = sellerId;
    this.finalPrice = finalPrice;
    this.depositAmount = depositAmount;
    this.status = status;
    this.resolvedAt = resolvedAt;
    this.consigneeName = consigneeName;
    this.phoneNumber = phoneNumber;
    this.address = address;
  }

  public Order(String auctionId, String buyerId, String sellerProfileId, BigDecimal finalPrice, BigDecimal depositAmount, BigDecimal remainingAmount, OrderStatus status, String consigneeName, String phoneNumber, String address) {
    this.auctionId = auctionId;
    this.buyerId = buyerId;
    this.sellerId = sellerProfileId;
    this.finalPrice = finalPrice;
    this.depositAmount = depositAmount;
    this.remainingAmount = remainingAmount;
    this.status = status;
    this.consigneeName = consigneeName;
    this.phoneNumber = phoneNumber;
    this.address = address;
  }

  public void confirm() {
    if (this.status != OrderStatus.PENDING) {
      throw new IllegalStateException("Chỉ có thể xác nhận đơn hàng đang PENDING");
    }
    this.status     = OrderStatus.CONFIRMED;
    this.resolvedAt = LocalDateTime.now();
  }

  public void cancel() {
    if (this.status != OrderStatus.PENDING) {
      throw new IllegalStateException("Chỉ có thể hủy đơn hàng đang PENDING");
    }
    this.status     = OrderStatus.CANCELLED;
    this.resolvedAt = LocalDateTime.now();
  }
}