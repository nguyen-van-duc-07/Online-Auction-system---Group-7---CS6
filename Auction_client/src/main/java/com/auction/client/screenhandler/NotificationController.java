package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.enums.NotificationType;
import com.auction.shared.model.notification.Notification;
import com.auction.shared.request.GetNotificationsRequestDTO;
import com.auction.shared.request.GetOrderRequestDTO;
import com.auction.shared.request.MarkNotificationReadRequestDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Bộ điều khiển (Controller) cho màn hình danh sách thông báo của người dùng (Notifications).
 * Quản lý việc hiển thị, đánh dấu đã đọc và điều hướng các tác vụ liên quan đến thông báo.
 */
public class NotificationController implements Initializable {
  /**
   * Biến static lưu trữ instance hiện tại của NotificationController để luồng mạng cập nhật UI.
   */
  public static NotificationController instance;

  @FXML private VBox notificationList;
  @FXML private VBox emptyDetail;
  @FXML private VBox detailContent;
  @FXML private HBox detailIconContainer;
  @FXML private Label detailTitle;
  @FXML private Label detailTime;
  @FXML private Label detailContent_;
  @FXML private Button actionButton;
  @FXML private Button deleteNotificationBtn;

  private Notification selectedNotification;

  /**
   * Khởi tạo bộ điều khiển thông báo.
   * Gán instance hiện tại vào biến static.
   *
   * @param location vị trí đường dẫn tương đối của đối tượng gốc
   * @param resources tài nguyên sử dụng để bản địa hóa đối tượng gốc
   */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;
  }

  /**
   * Tải danh sách thông báo của người dùng lên giao diện.
   *
   * @param notifications danh sách thông báo
   * @param unreadCount số thông báo chưa đọc
   */
  public void loadNotifications(List<Notification> notifications, int unreadCount) {
    notificationList.getChildren().clear();

    if (notifications.isEmpty()) {
      Label empty = new Label("Không có thông báo nào");
      empty.setStyle("-fx-text-fill: #888; -fx-font-size: 13px; -fx-padding: 20;");
      notificationList.getChildren().add(empty);
      return;
    }

    for (Notification n : notifications) {
      notificationList.getChildren().add(buildNotificationRow(n));
    }
  }

  private HBox buildNotificationRow(Notification n) {
    HBox row = new HBox(10);
    row.setPadding(new Insets(12, 15, 12, 15));
    row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    row.setStyle(n.isRead()
        ? "-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"
        : "-fx-background-color: #e8f5e9; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"
    );

    // Chấm đỏ nếu chưa đọc
    Label dot = new Label("●");
    dot.setStyle("-fx-text-fill: " + (n.isRead() ? "transparent" : "red") + "; -fx-font-size: 8px;");

    // Icon
    FontIcon icon = getIcon(n.getType());

    // Title + time
    VBox textBox = new VBox(3);
    HBox.setHgrow(textBox, Priority.ALWAYS);

    Label title = new Label(n.getTitle());
    title.setStyle(n.isRead()
        ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;"
        : "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1e7d32;"
    );
    title.setWrapText(true);

    Label time = new Label(n.getCreatedAt()
        .format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
    time.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");

    textBox.getChildren().addAll(title, time);
    row.getChildren().addAll(dot, icon, textBox);

    // Hover effect
    row.setOnMouseEntered(e ->
        row.setStyle(row.getStyle() + "-fx-background-color: #f5f5f5;")
    );
    row.setOnMouseExited(e ->
        row.setStyle(n.isRead()
            ? "-fx-background-color: white; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"
            : "-fx-background-color: #e8f5e9; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; -fx-cursor: hand;"
        )
    );

    row.setOnMouseClicked(e -> showDetail(n));
    return row;
  }

  private void showDetail(Notification n) {
    selectedNotification = n;

    // Đánh dấu đã đọc nếu chưa
    if (!n.isRead()) {
      ServerConnection.sendData(new MarkNotificationReadRequestDTO(n.getId()));
      n.setRead(true);
      // Refresh list
      ServerConnection.sendData(
          new GetNotificationsRequestDTO(SessionManager.getCurrentUser().getId())
      );
    }

    // Hiện detail panel
    emptyDetail.setVisible(false);
    emptyDetail.setManaged(false);
    detailContent.setVisible(true);
    detailContent.setManaged(true);

    if (deleteNotificationBtn != null) {
      deleteNotificationBtn.setDisable(false);
    }

    // Fill data
    detailIconContainer.getChildren().clear();
    FontIcon icon = getIcon(n.getType());
    icon.setIconSize(28);
    detailIconContainer.getChildren().add(icon);
    detailTitle.setText(n.getTitle());
    detailTime.setText(n.getCreatedAt()
        .format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")));
    detailContent_.setText(n.getContent());

    // Hiện nút action nếu có referenceId
    boolean hasAction = n.getReferenceId() != null
        && (n.getType() == NotificationType.AUCTION_WON
        || n.getType() == NotificationType.ORDER_CONFIRMED
        || n.getType() == NotificationType.ORDER_CANCELLED_BY_BUYER
        || n.getType() == NotificationType.ORDER_CANCELLED);

    actionButton.setVisible(hasAction);
    actionButton.setManaged(hasAction);

    if (hasAction) {
      actionButton.setText(n.getType() == NotificationType.AUCTION_WON
          ? "Xem đơn hàng & Thanh toán"
          : "Xem chi tiết đơn hàng"
      );
    }
  }

  /**
   * Xử lý sự kiện nhấn nút hành động đi kèm thông báo (ví dụ: đi đến thanh toán).
   */
  @FXML
  public void handleAction() {
    if (selectedNotification == null) return;
    ServerConnection.sendData(
        new GetOrderRequestDTO(selectedNotification.getReferenceId())
    );
    notificationList.getScene().getWindow().hide();
  }

  /**
   * Yêu cầu hệ thống đánh dấu tất cả các thông báo hiện có là đã đọc.
   */
  @FXML
  public void markAllAsRead() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new MarkNotificationReadRequestDTO(userId, true));
    ServerConnection.sendData(new GetNotificationsRequestDTO(userId));

    MainLayoutController mainLayoutController = MainLayoutController.getInstance();
    if (mainLayoutController != null) {
      mainLayoutController.updateNotificationBadge(0);
    }
  }

  private FontIcon getIcon(NotificationType type) {
    FontIcon icon = switch (type) {
      case AUCTION_WON               -> new FontIcon("fas-trophy");
      case ORDER_CONFIRMED           -> new FontIcon("fas-check-circle");
      case ORDER_CANCELLED,
           ORDER_CANCELLED_BY_BUYER  -> new FontIcon("fas-times-circle");
      case AUCTION_ENDED             -> new FontIcon("fas-bell");
      case REQUEST_APPROVED          -> new FontIcon("fas-check-circle");
      case REQUEST_REJECTED          -> new FontIcon("fas-times-circle");
      case REQUEST_SUBMITTED         -> new FontIcon("fas-hourglass-half");
      case AUCTION_CANCELLED         -> new FontIcon("fas-ban");
      case SYSTEM                    -> new FontIcon("fas-bullhorn");
    };

    // Màu theo type
    Color color = switch (type) {
      case AUCTION_WON, REQUEST_APPROVED, ORDER_CONFIRMED -> Color.web("#27ae60");
      case ORDER_CANCELLED, ORDER_CANCELLED_BY_BUYER,
           REQUEST_REJECTED, AUCTION_CANCELLED            -> Color.web("#e74c3c");
      case AUCTION_ENDED                                  -> Color.web("#3498db");
      case REQUEST_SUBMITTED                              -> Color.web("#f39c12");
      case SYSTEM                                         -> Color.web("#9b59b6");
    };

    icon.setIconColor(color);
    icon.setIconSize(20);
    return icon;
  }

  /**
   * Xóa thông báo đang được chọn hiện tại khỏi hệ thống.
   */
  @FXML
  public void deleteSelectedNotification() {
    if (selectedNotification == null) return;

    ServerConnection.sendData(new com.auction.shared.request.DeleteNotificationRequestDTO(selectedNotification.getId()));

    selectedNotification = null;
    if (deleteNotificationBtn != null) {
      deleteNotificationBtn.setDisable(true);
    }
    emptyDetail.setVisible(true);
    emptyDetail.setManaged(true);
    detailContent.setVisible(false);
    detailContent.setManaged(false);

    ServerConnection.sendData(
        new GetNotificationsRequestDTO(SessionManager.getCurrentUser().getId())
    );
  }
}
