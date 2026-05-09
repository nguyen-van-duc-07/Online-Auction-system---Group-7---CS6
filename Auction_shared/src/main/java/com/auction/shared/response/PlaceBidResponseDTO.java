package com.auction.shared.response;

public class PlaceBidResponseDTO implements ResponseDTO{
  private boolean success;

  private String message;

  public PlaceBidResponseDTO(
      boolean success,
      String message
  ) {
    this.success = success;
    this.message = message;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }
}
