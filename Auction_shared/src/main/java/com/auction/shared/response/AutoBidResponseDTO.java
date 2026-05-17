package com.auction.shared.response;

import java.io.Serializable;

public class AutoBidResponseDTO implements ResponseDTO {
  private boolean success;
  private String message;

  public AutoBidResponseDTO(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public boolean isSuccess() { return success; }
  public String getMessage() { return message; }
}