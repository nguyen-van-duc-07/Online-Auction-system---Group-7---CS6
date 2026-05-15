package com.auction.shared.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
@Getter
@Setter
public class PaymentNotificationDTO implements ResponseDTO {
  private String orderId;
  private String itemName;
  private BigDecimal finalPrice;

  public PaymentNotificationDTO(String orderId,
                                String itemName,
                                BigDecimal finalPrice) {
    this.orderId = orderId;
    this.itemName   = itemName;
    this.finalPrice = finalPrice;
  }
}