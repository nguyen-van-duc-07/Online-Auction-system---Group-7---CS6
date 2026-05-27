package com.auction.shared.request;

public class GetOrderRequestDTO implements RequestDTO {
  private static final long serialVersionUID = 1L;
  private String orderId;

  public GetOrderRequestDTO(String orderId) {
    this.orderId = orderId;
  }

  public String getOrderId() { return orderId; }
}