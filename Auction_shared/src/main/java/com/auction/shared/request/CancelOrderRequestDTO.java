package com.auction.shared.request;

import lombok.Getter;

@Getter
public class CancelOrderRequestDTO implements RequestDTO {
  private String orderId;

  public CancelOrderRequestDTO(String orderId) {
    this.orderId = orderId;
  }

}