package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.AuctionRequestDTO;
import com.auction.shared.request.GetOrderRequestDTO;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bộ điều khiển (Controller) cho thẻ hiển thị đơn hàng kết quả đấu giá (OrderCard).
 * Quản lý trạng thái đếm ngược tự động hủy đơn, chuyển hướng đến thanh toán và xem chi tiết sản phẩm.
 */
public class OrderCardController {
  private static final Logger log = LoggerFactory.getLogger(OrderCardController.class);
  @FXML
  private Label itemNameLabel;

  @FXML
  private Label finalPriceLabel;

  @FXML
  private Label brandNameLabel;

  @FXML
  private Label winnerNameLabel;

  @FXML
  private javafx.scene.layout.HBox cardContainer;

  @FXML
  private javafx.scene.shape.Rectangle statusIndicator;

  @FXML
  private Label timeRemainingLabel;

  @FXML
  private javafx.scene.control.Button btnPayment;

  private OrderDTO currentOrder;
  private Timeline countdownTimeline;

  @FXML
  public void initialize() {
    if (statusIndicator != null && cardContainer != null) {
      statusIndicator.heightProperty().bind(cardContainer.heightProperty());
    }
  }
 
  /**
   * Chuyển hướng người dùng sang trang thanh toán chi tiết của đơn hàng.
   */
  @FXML
  public void gotoPayment() {
    SessionManager.setCurrentOrderId(currentOrder.getOrderId());
    log.info("Đang mở chi tiết phiên đơn hàng: {}", currentOrder.getAuctionId());
    ServerConnection.sendData(new GetOrderRequestDTO(currentOrder.getOrderId()));
  }

  /**
   * Chuyển hướng người dùng sang màn hình xem thông tin chi tiết sản phẩm.
   */
  @FXML
  public void gotoItemDetail() {
    String auctionId = currentOrder.getAuctionId();
    com.auction.shared.request.AuctionRequestDTO auctionRequestDTO = new com.auction.shared.request.AuctionRequestDTO();
    auctionRequestDTO.setAuctionId(auctionId);
    ServerConnection.sendData(auctionRequestDTO);
  }

  /**
   * Bơm dữ liệu để hiển thị thông tin thẻ đơn hàng.
   *
   * @param order đối tượng DTO chứa thông tin đơn hàng
   */
  public void setData(OrderDTO order) {
    this.currentOrder = order;
    
    // Đổ dữ liệu text
    itemNameLabel.setText(order.getItemName());

    String formattedPrice = String.format("%,.0f VNĐ", order.getFinalPrice());
    finalPriceLabel.setText(formattedPrice);

    if (order.getBrandName() != null && !order.getBrandName().trim().isEmpty()) {
      brandNameLabel.setText("Người bán: " + order.getBrandName());
    } else {
      brandNameLabel.setText("Người bán: N/A");
    }

    if (order.getWinnerName() != null && !order.getWinnerName().trim().isEmpty()) {
      winnerNameLabel.setText(order.getWinnerName());
    } else {
      winnerNameLabel.setText("Không rõ");
    }

    // Dừng timer cũ nếu có
    if (countdownTimeline != null) {
      countdownTimeline.stop();
    }

    // Xác định vai trò của người dùng hiện tại đối với đơn hàng này
    boolean isSeller = false;
    UserDTO currentUser = SessionManager.getCurrentUser();
    if (currentUser != null && order.getSellerId() != null) {
      isSeller = currentUser.getId().equals(order.getSellerId());
    }

    com.auction.shared.enums.OrderStatus status = order.getStatus();

    if (isSeller) {
      // --- NGƯỜI BÁN ---
      if (status == com.auction.shared.enums.OrderStatus.CANCELLED) {
        // Trạng thái đơn bị hủy -> Đổi nền màu đỏ nhẹ nổi bật
        cardContainer.setStyle("-fx-background-color: #FEE2E2; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(239,68,68,0.2), 16, 0, 0, 4);");
        statusIndicator.setFill(javafx.scene.paint.Color.web("#EF4444"));
        timeRemainingLabel.setText("✕ Đơn hàng đã bị hủy");
        timeRemainingLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");

        btnPayment.setText("Đã hủy ✕");
        btnPayment.setDisable(true);
        btnPayment.setStyle("-fx-background-color: #94A3B8; -fx-background-radius: 20; -fx-cursor: default;");
      } else {
        // Trạng thái bình thường (PENDING hoặc CONFIRMED)
        cardContainer.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(148,163,184,0.12), 16, 0, 0, 4);");
        statusIndicator.setFill(javafx.scene.paint.Color.web("#009900"));

        btnPayment.setText("Xem hóa đơn 📄");
        btnPayment.setDisable(false);
        btnPayment.setStyle("-fx-background-color: #4F46E5; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(79,70,229,0.2), 8, 0, 0, 2);");

        if (status == com.auction.shared.enums.OrderStatus.PENDING) {
          startCountdown(order.getCreatedAt());
        } else {
          timeRemainingLabel.setText("✓ Đã xác nhận thanh toán");
          timeRemainingLabel.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
        }
      }
    } else {
      // --- NGƯỜI MUA ---
      if (status == com.auction.shared.enums.OrderStatus.CANCELLED) {
        // Trạng thái đơn bị hủy -> Đổi nền màu đỏ nhẹ nổi bật
        cardContainer.setStyle("-fx-background-color: #FEE2E2; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(239,68,68,0.2), 16, 0, 0, 4);");
        statusIndicator.setFill(javafx.scene.paint.Color.web("#EF4444"));
        timeRemainingLabel.setText("✕ Đơn hàng đã bị hủy");
        timeRemainingLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");

        btnPayment.setText("Đã hủy ✕");
        btnPayment.setDisable(true);
        btnPayment.setStyle("-fx-background-color: #94A3B8; -fx-background-radius: 20; -fx-cursor: default;");
      } else {
        // Trạng thái bình thường
        cardContainer.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(148,163,184,0.12), 16, 0, 0, 4);");
        statusIndicator.setFill(javafx.scene.paint.Color.web("#009900"));

        if (status == com.auction.shared.enums.OrderStatus.PENDING) {
          btnPayment.setText("Thanh toán 🛒");
          btnPayment.setDisable(false);
          btnPayment.setStyle("-fx-background-color: #009900; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,153,0,0.2), 8, 0, 0, 2);");
          startCountdown(order.getCreatedAt());
        } else {
          // Trạng thái CONFIRMED (hoàn tất)
          btnPayment.setText("Xem đơn hàng 👁");
          btnPayment.setDisable(false);
          btnPayment.setStyle("-fx-background-color: #0284C7; -fx-background-radius: 20; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(2,132,199,0.2), 8, 0, 0, 2);");
          timeRemainingLabel.setText("✓ Đã xác nhận thanh toán");
          timeRemainingLabel.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");
        }
      }
    }
  }

  private void startCountdown(java.time.LocalDateTime createdAt) {
    if (createdAt == null) {
      timeRemainingLabel.setText("⏳ Đang cập nhật thời gian...");
      timeRemainingLabel.setStyle("-fx-text-fill: #64748B;");
      return;
    }

    java.time.LocalDateTime deadline = createdAt.plusDays(7);

    Runnable updateTime = () -> {
      java.time.LocalDateTime now = java.time.LocalDateTime.now();
      java.time.Duration duration = java.time.Duration.between(now, deadline);
      if (duration.isNegative() || duration.isZero()) {
        timeRemainingLabel.setText("⏳ Hết hạn xác nhận (Tự động hủy)");
        timeRemainingLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
        if (countdownTimeline != null) {
          countdownTimeline.stop();
        }
      } else {
        timeRemainingLabel.setText("⏳ Tự động hủy sau: " + CountdownHelper.formatDuration(duration));
        timeRemainingLabel.setStyle("-fx-text-fill: #D97706;"); // Màu cam hổ phách
      }
    };

    updateTime.run(); // Cập nhật lần đầu ngay lập tức

    countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime.run()));
    countdownTimeline.setCycleCount(Animation.INDEFINITE);
    countdownTimeline.play();
  }
}
