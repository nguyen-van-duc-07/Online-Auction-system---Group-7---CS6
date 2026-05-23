package com.auction.shared.request;

import lombok.Getter;

@Getter
public class GetCompletedOrdersOfSellerRequestDTO implements RequestDTO {
  private String sellerId;

  public GetCompletedOrdersOfSellerRequestDTO(String sellerId) {
    this.sellerId = sellerId;
  }
}
