package com.auction.shared.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteNotificationResponseDTO implements ResponseDTO {
  private boolean success;
  private String message;

  public DeleteNotificationResponseDTO() {}

  public DeleteNotificationResponseDTO(boolean success, String message) {
    this.success = success;
    this.message = message;
  }
}
