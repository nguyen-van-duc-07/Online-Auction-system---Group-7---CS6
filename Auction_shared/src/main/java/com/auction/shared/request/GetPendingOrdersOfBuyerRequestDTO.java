package com.auction.shared.request;

import lombok.Getter;

@Getter
public class GetPendingOrdersOfBuyerRequestDTO implements RequestDTO {
  private String buyerId;

  public GetPendingOrdersOfBuyerRequestDTO(String buyerId) {
    this.buyerId = buyerId;
  }
}
