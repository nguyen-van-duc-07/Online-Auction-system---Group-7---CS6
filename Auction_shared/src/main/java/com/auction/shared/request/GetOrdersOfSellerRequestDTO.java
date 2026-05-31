package com.auction.shared.request;

import com.auction.shared.enums.OrderStatus;
import lombok.Getter;

@Getter
public class GetOrdersOfSellerRequestDTO implements RequestDTO {
  private String sellerId;
  private OrderStatus status;

  public GetOrdersOfSellerRequestDTO(String sellerId, OrderStatus status) {
    this.sellerId = sellerId;
    this.status = status;
  }
}
