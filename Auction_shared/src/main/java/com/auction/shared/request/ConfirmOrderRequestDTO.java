package com.auction.shared.request;

import lombok.Getter;

@Getter
public class ConfirmOrderRequestDTO implements RequestDTO {
  private String orderId;

  public ConfirmOrderRequestDTO(String orderId) {
    this.orderId = orderId;
  }

}