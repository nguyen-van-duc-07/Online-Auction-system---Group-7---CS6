package com.auction.shared.request;


public class GetNotificationsRequestDTO implements RequestDTO {
  private String userId;

  public GetNotificationsRequestDTO(String userId) {
    this.userId = userId;
  }

  public String getUserId() { return userId; }
}