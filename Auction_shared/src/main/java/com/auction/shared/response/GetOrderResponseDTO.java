package com.auction.shared.response;

import com.auction.shared.model.order.Order;

public class GetOrderResponseDTO implements ResponseDTO {
  private boolean success;
  private String message;
  private Order order;

  public GetOrderResponseDTO(boolean success, String message, Order order) {
    this.success = success;
    this.message = message;
    this.order   = order;
  }

  public boolean isSuccess() { return success; }
  public String getMessage() { return message; }
  public Order getOrder()    { return order; }
}