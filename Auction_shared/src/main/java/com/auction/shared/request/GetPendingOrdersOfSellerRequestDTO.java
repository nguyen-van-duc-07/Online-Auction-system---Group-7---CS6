package com.auction.shared.request;

import lombok.Getter;

@Getter
public class GetPendingOrdersOfSellerRequestDTO implements RequestDTO {
  private String userId;

  public GetPendingOrdersOfSellerRequestDTO(String userId) {
    this.userId = userId;
  }
}
