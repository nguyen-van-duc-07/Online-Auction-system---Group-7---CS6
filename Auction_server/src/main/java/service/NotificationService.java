package service;

import com.auction.shared.enums.NotificationType;
import com.auction.shared.model.notification.Notification;
import repository.NotificationRepository;
import servercontroller.Server;
import com.auction.shared.response.NotificationDTO;

import java.util.List;

public class NotificationService {
  private final NotificationRepository notifRepo = new NotificationRepository();

  /**
   * Tạo và lưu thông báo, sau đó gửi realtime cho user nếu đang online.
   */
  public void send(String userId, NotificationType type,
                   String title, String content, String referenceId) {
    // 1. Lưu vào DB
    Notification notification = new Notification(userId, type, title, content, referenceId);
    boolean saved = notifRepo.save(notification);

    if (!saved) {
      System.out.println("[NOTIFICATION] Lưu thông báo thất bại cho user: " + userId);
      return;
    }

    // 2. Gửi realtime nếu user đang online
    Server.sendToUser(userId, new NotificationDTO(
        notification.getId(),
        notification.getType(),
        notification.getTitle(),
        notification.getContent(),
        notification.getReferenceId(),
        notification.isRead(),
        notification.getCreatedAt()
    ));

    System.out.println("[NOTIFICATION] Đã gửi thông báo cho user: " + userId
        + " | Type: " + type
        + " | Title: " + title);
  }

  public List<Notification> getNotifications(String userId) {
    return notifRepo.findByUserId(userId);
  }

  public int getUnreadCount(String userId) {
    return notifRepo.countUnread(userId);
  }

  public boolean markAsRead(String notificationId) {
    return notifRepo.markAsRead(notificationId);
  }

  public boolean markAllAsRead(String userId) {
    return notifRepo.markAllAsRead(userId);
  }

  public void deleteExpired() {
    notifRepo.deleteExpired();
  }
}