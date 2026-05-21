package com.auction.shared.request;

import lombok.Getter;

@Getter
public class GetPendingOrdersOfSellerRequestDTO implements RequestDTO {
  private String sellerId;

  public GetPendingOrdersOfSellerRequestDTO(String sellerId) {
    this.sellerId = sellerId;
  }
}
