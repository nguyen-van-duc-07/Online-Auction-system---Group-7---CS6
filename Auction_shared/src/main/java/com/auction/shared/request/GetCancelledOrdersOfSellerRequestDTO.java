package com.auction.shared.request;

import lombok.Getter;

@Getter
public class GetCancelledOrdersOfSellerRequestDTO implements RequestDTO {
  private String sellerId;

  public GetCancelledOrdersOfSellerRequestDTO(String sellerId) {
    this.sellerId = sellerId;
  }
}
