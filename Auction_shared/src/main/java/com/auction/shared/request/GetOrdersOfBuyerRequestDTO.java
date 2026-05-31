package com.auction.shared.request;

import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.order.Order;
import lombok.Getter;

@Getter
public class GetOrdersOfBuyerRequestDTO implements RequestDTO {
  private String buyerId;
  private OrderStatus status;

  public GetOrdersOfBuyerRequestDTO(String buyerId, OrderStatus status) {
    this.status = status;
    this.buyerId = buyerId;
  }
}
