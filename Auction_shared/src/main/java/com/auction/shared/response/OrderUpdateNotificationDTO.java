package com.auction.shared.response;

import com.auction.shared.enums.OrderStatus;
import lombok.Getter;

import java.math.BigDecimal;
@Getter
public class OrderUpdateNotificationDTO implements ResponseDTO {
  private String orderId;
  private OrderStatus status;

  public OrderUpdateNotificationDTO(String orderId,
                                    OrderStatus status) {
    this.orderId    = orderId;
    this.status     = status;
  }
}