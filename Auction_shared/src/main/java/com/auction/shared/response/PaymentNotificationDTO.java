package com.auction.shared.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
@Getter
@Setter
public class PaymentNotificationDTO implements ResponseDTO {
  private String auctionId;
  private String itemName;
  private BigDecimal finalPrice;
  private String winnerId;

  public PaymentNotificationDTO(String auctionId,
                                String itemName,
                                BigDecimal finalPrice,
                                String winnerId) {
    this.auctionId = auctionId;
    this.itemName   = itemName;
    this.finalPrice = finalPrice;
    this.winnerId   = winnerId;
  }

  // getters...
}