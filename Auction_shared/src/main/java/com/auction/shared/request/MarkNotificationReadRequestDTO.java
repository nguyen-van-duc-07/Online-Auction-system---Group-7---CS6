package com.auction.shared.request;

import lombok.Getter;

@Getter
public class MarkNotificationReadRequestDTO implements RequestDTO {
  private String notificationId; // null nếu muốn đánh dấu tất cả
  private String userId;
  private boolean markAll;

  // Đánh dấu 1 thông báo
  public MarkNotificationReadRequestDTO(String notificationId) {
    this.notificationId = notificationId;
    this.markAll = false;
  }

  // Đánh dấu tất cả
  public MarkNotificationReadRequestDTO(String userId, boolean markAll) {
    this.userId  = userId;
    this.markAll = markAll;
  }

}