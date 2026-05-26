package com.auction.shared.response;

public class ChangePasswordResponseDTO implements ResponseDTO {
  private boolean success;
  private String message;

  public ChangePasswordResponseDTO(boolean success, String message) {
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
