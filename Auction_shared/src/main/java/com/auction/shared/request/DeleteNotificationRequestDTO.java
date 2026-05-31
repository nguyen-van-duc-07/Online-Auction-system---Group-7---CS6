package com.auction.shared.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteNotificationRequestDTO implements RequestDTO {
  private String notificationId;

  public DeleteNotificationRequestDTO() {}

  public DeleteNotificationRequestDTO(String notificationId) {
    this.notificationId = notificationId;
  }
}
