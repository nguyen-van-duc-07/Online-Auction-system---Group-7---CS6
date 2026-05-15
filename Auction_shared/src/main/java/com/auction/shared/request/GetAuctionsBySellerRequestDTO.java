package com.auction.shared.request;

public class GetAuctionsBySellerRequestDTO implements RequestDTO {
  private String userId;

  public GetAuctionsBySellerRequestDTO(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
