package com.auction.shared.model.notification;

import com.auction.shared.enums.NotificationType;
import com.auction.shared.model.core.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class Notification extends Entity {
  private String userId;
  private NotificationType type;
  private String title;
  private String content;
  private String referenceId; // orderId hoặc auctionId để navigate
  private boolean read;

  public Notification(String userId, NotificationType type,
                      String title, String content, String referenceId) {
    this.userId      = userId;
    this.type        = type;
    this.title       = title;
    this.content     = content;
    this.referenceId = referenceId;
    this.read        = false;
  }
}