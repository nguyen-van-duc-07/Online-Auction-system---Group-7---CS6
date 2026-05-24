package com.auction.shared.request;

import lombok.Getter;

@Getter
public class GetCompletedOrdersOfBuyerRequestDTO implements RequestDTO {
  private String buyerId;

  public GetCompletedOrdersOfBuyerRequestDTO(String buyerId) {
    this.buyerId = buyerId;
  }
}
