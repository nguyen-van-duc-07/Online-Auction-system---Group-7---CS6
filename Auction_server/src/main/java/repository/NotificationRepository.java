package repository;

import com.auction.shared.enums.NotificationType;
import com.auction.shared.model.notification.Notification;
import config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

  public boolean save(Notification notification) {
    String sql = "INSERT INTO notifications (id, user_id, type, title, content, reference_id, is_read, created_at) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, notification.getId());
      ps.setString(2, notification.getUserId());
      ps.setString(3, notification.getType().name());
      ps.setString(4, notification.getTitle());
      ps.setString(5, notification.getContent());
      ps.setString(6, notification.getReferenceId());
      ps.setBoolean(7, notification.isRead());

      return ps.executeUpdate() > 0;

    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  // Lấy tất cả thông báo của user, mới nhất trước
  public List<Notification> findByUserId(String userId) {
    List<Notification> notifications = new ArrayList<>();
    String sql = "SELECT * FROM notifications "
        + "WHERE user_id = ? "
        + "ORDER BY is_read ASC, created_at DESC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, userId);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        notifications.add(mapRow(rs));
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return notifications;
  }

  // Đếm số thông báo chưa đọc
  public int countUnread(String userId) {
    String sql = "SELECT COUNT(*) FROM notifications "
        + "WHERE user_id = ? AND is_read = FALSE";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, userId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) return rs.getInt(1);

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return 0;
  }

  // Đánh dấu 1 thông báo đã đọc
  public boolean markAsRead(String notificationId) {
    String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, notificationId);
      return ps.executeUpdate() > 0;

    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  // Đánh dấu tất cả đã đọc
  public boolean markAllAsRead(String userId) {
    String sql = "UPDATE notifications SET is_read = TRUE "
        + "WHERE user_id = ? AND is_read = FALSE";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, userId);
      return ps.executeUpdate() > 0;

    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  // Xóa thông báo quá 30 ngày
  public void deleteExpired() {
    String sql = "DELETE FROM notifications "
        + "WHERE created_at <= DATE_SUB(NOW(), INTERVAL 30 DAY)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      int deleted = ps.executeUpdate();
      if (deleted > 0) {
        System.out.println("[NOTIFICATION] Đã xóa " + deleted + " thông báo hết hạn.");
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private Notification mapRow(ResultSet rs) throws SQLException {
    Notification n = new Notification();
    n.setId(rs.getString("id"));
    n.setUserId(rs.getString("user_id"));
    n.setType(NotificationType.valueOf(rs.getString("type")));
    n.setTitle(rs.getString("title"));
    n.setContent(rs.getString("content"));
    n.setReferenceId(rs.getString("reference_id"));
    n.setRead(rs.getBoolean("is_read"));
    n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    return n;
  }
  public static int countUnreadFromList(List<Notification> notifications) {
    return (int) notifications.stream().filter(n -> !n.isRead()).count();
  }
}