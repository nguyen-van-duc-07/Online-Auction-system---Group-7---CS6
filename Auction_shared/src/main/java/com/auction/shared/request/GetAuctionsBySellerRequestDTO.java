package com.auction.shared.request;

import lombok.Getter;

@Getter
public class GetAuctionsBySellerRequestDTO implements RequestDTO {
  private String userId;

  public GetAuctionsBySellerRequestDTO(String userId) {
    this.userId = userId;
  }

}
