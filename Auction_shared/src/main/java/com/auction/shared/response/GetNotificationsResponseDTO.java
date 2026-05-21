package com.auction.shared.response;

import com.auction.shared.model.notification.Notification;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class GetNotificationsResponseDTO implements ResponseDTO, Serializable {
  private boolean success;
  private List<Notification> notifications;
  private int unreadCount;

  public GetNotificationsResponseDTO(boolean success,
                                     List<Notification> notifications,
                                     int unreadCount) {
    this.success       = success;
    this.notifications = notifications;
    this.unreadCount   = unreadCount;
  }
}