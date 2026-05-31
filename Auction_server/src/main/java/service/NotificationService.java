package service;

import com.auction.shared.enums.NotificationType;
import com.auction.shared.model.notification.Notification;
import repository.NotificationRepository;
import servercontroller.Server;
import com.auction.shared.response.NotificationDTO;

import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDateTime;
import com.auction.shared.util.CurrencyUtils;

public class NotificationService {
  private final NotificationRepository notifRepo;

  private static class Holder {
    private static final NotificationService INSTANCE = new NotificationService();
  }

  public static NotificationService getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * Constructor mặc định cho Production.
   */
  private NotificationService() {
    this(NotificationRepository.getInstance());
  }

  /**
   * Constructor nhận tham số phục vụ cho Unit Test.
   */
  public NotificationService(NotificationRepository notifRepo) {
    this.notifRepo = notifRepo;
  }

  /**
   * Tạo và lưu thông báo, sau đó gửi realtime cho user nếu đang online.
   */
  public void sendFromNotification(Notification notification) {
    boolean saved = notifRepo.save(notification);
    if (!saved) return;

    Server.sendToUser(notification.getUserId(), new NotificationDTO(
        notification.getId(),
        notification.getType(),
        notification.getTitle(),
        notification.getContent(),
        notification.getReferenceId(),
        notification.isRead(),
        notification.getCreatedAt()
    ));
  }

  /**
   * Tạo gói tin thông báo và phát realtime đến tất cả bidder đang online khi có phiên đấu giá mở.
   */
  public void sendNewAuctionNotification(String auctionId, String itemName, BigDecimal startPrice) {
    NotificationDTO dto = new NotificationDTO(
        UUID.randomUUID().toString(),
        NotificationType.SYSTEM,
        "🎉 Phiên đấu giá mới mở!",
        "Sản phẩm '" + itemName + "' đã bắt đầu đấu giá với giá khởi điểm "
            + CurrencyUtils.formatVnd(startPrice) + " VNĐ. Tham gia ngay!",
        auctionId,
        true,
        LocalDateTime.now()
    );

    Server.broadcastToAll(dto);
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