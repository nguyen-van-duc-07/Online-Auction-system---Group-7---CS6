package com.auction.shared.request;

public class CheckingSellerProfileRequestDTO implements RequestDTO {
  private String userId;

  public CheckingSellerProfileRequestDTO(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
