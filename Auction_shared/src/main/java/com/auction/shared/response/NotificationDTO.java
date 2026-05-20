package com.auction.shared.response;

import com.auction.shared.enums.NotificationType;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
@Getter
public class NotificationDTO implements ResponseDTO {
  private String id;
  private NotificationType type;
  private String title;
  private String content;
  private String referenceId;
  private boolean read;
  private LocalDateTime createdAt;

  public NotificationDTO(String id, NotificationType type,
                         String title, String content,
                         String referenceId, boolean read,
                         LocalDateTime createdAt) {
    this.id          = id;
    this.type        = type;
    this.title       = title;
    this.content     = content;
    this.referenceId = referenceId;
    this.read        = read;
    this.createdAt   = createdAt;
  }
}