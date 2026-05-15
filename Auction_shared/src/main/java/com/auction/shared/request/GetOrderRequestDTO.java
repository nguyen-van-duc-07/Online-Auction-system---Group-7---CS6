package com.auction.shared.request;

public class GetOrderRequestDTO implements RequestDTO {
  private String orderId;

  public GetOrderRequestDTO(String orderId) {
    this.orderId = orderId;
  }

  public String getOrderId() { return orderId; }
}