package com.auction.shared.request;

import lombok.Getter;

@Getter
public class GetCancelledOrdersOfBuyerRequestDTO implements RequestDTO {
  private String buyerId;

  public GetCancelledOrdersOfBuyerRequestDTO(String buyerId) {
    this.buyerId = buyerId;
  }
}
