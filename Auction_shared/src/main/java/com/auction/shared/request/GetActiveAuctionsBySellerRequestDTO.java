package com.auction.shared.request;

import lombok.Getter;

@Getter
public class GetActiveAuctionsBySellerRequestDTO implements RequestDTO {
  private String userId;

  public GetActiveAuctionsBySellerRequestDTO(String userId) {
    this.userId = userId;
  }
}
