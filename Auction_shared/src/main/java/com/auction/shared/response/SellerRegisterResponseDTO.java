package com.auction.shared.response;

public class SellerRegisterResponseDTO implements ResponseDTO {
  private boolean success;
  private String message;

  public SellerRegisterResponseDTO(boolean success, String message) {
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
