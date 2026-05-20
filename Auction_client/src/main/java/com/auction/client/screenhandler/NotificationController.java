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

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class NotificationController implements Initializable {
  public static NotificationController instance;

  @FXML private VBox notificationList;
  @FXML private VBox emptyDetail;
  @FXML private VBox detailContent;
  @FXML private Label detailIcon;
  @FXML private Label detailTitle;
  @FXML private Label detailTime;
  @FXML private Label detailContent_;
  @FXML private Button actionButton;

  private Notification selectedNotification;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;
  }

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
    Label icon = new Label(getIcon(n.getType()));
    icon.setStyle("-fx-font-size: 16px;");

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

    // Fill data
    detailIcon.setText(getIcon(n.getType()));
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

  @FXML
  public void handleAction() {
    if (selectedNotification == null) return;
    ServerConnection.sendData(
        new GetOrderRequestDTO(selectedNotification.getReferenceId())
    );
    notificationList.getScene().getWindow().hide();
  }

  @FXML
  public void markAllAsRead() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new MarkNotificationReadRequestDTO(userId, true));
    ServerConnection.sendData(new GetNotificationsRequestDTO(userId));

    HomeController homeController = HomeController.getInstance();
    if (homeController != null) {
      homeController.updateNotificationBadge(0);
    }
  }

  private String getIcon(NotificationType type) {
    return switch (type) {
      case AUCTION_WON               -> "🏆";
      case OUTBID                    -> "🔥";
      case ORDER_CONFIRMED           -> "✅";
      case ORDER_CANCELLED,
           ORDER_CANCELLED_BY_BUYER  -> "❌";
      case AUCTION_ENDED             -> "🔔";
      case SELLER_APPROVED           -> "✅";
      case SELLER_REJECTED           -> "❌";
      case AUCTION_CANCELLED         -> "🚫";
      case SYSTEM                    -> "ℹ️";
    };
  }
}