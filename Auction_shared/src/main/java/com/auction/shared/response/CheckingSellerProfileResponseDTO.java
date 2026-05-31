package com.auction.shared.response;


public class CheckingSellerProfileResponseDTO implements ResponseDTO {
  private String message;
  private boolean success;

  public CheckingSellerProfileResponseDTO(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public boolean isSuccess() {
    return success;
  }
}
