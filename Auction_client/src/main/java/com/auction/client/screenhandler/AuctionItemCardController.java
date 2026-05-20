package com.auction.client.screenhandler;

import com.auction.client.network.SessionManager;
import com.auction.shared.enums.AuctionStatusUI;
import com.auction.shared.model.auction.AuctionDTO;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;

public class AuctionItemCardController {

  @FXML
  private Label nameLabel;
  @FXML
  private Label statusLabel;
  @FXML
  private Label priceLabel;
  @FXML
  private Label timeLabel;

  private Controller currentScreen;
  private Timeline countdownTimer;
  private AuctionDTO auction;

  /**
   * Hàm này dùng để bơm dữ liệu từ một Controller cha truyền sang.
   *
   */
  public void setData(AuctionDTO auction, Controller currentScreen) {
    this.currentScreen = currentScreen;
    this.auction = auction;
    // 1. Đổ dữ liệu text
    nameLabel.setText(auction.getItemName());

    AuctionStatusUI status = AuctionStatusUI.fromShared(auction.getStatus());
    statusLabel.setText(status.getDisplayName());
    statusLabel.setStyle("-fx-text-fill: " + status.getColor() + "; -fx-font-weight: bold;");

    String formattedPrice = String.format("%,.0f VNĐ", auction.getCurrentPrice());
    priceLabel.setText(formattedPrice);

    // Tạo bộ đếm thời gian chạy mỗi 1 giây
    countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime startTime = auction.getStartTime();
      LocalDateTime endTime = auction.getEndTime();

      if (startTime != null && now.isBefore(startTime)) {
        // TRƯỜNG HỢP 1: Phiên đấu giá chưa bắt đầu (WAITING)
        String timeLeft = formatTimeLeft(now, startTime);
        timeLabel.setText(timeLeft);
        timeLabel.setStyle("-fx-text-fill: #f39c12;"); // Màu cam nhắc nhở

        // Có thể update luôn Label trạng thái nếu trước đó server gửi về WAITING
        statusLabel.setText("SẮP DIỄN RA");
        statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");

      } else if (now.isAfter(startTime) && now.isBefore(endTime)) {
        // TRƯỜNG HỢP 2: Phiên đấu giá đang diễn ra (ACTIVE)
        String timeLeft = formatTimeLeft(now, endTime);
        timeLabel.setText(timeLeft);
        timeLabel.setStyle("-fx-text-fill: #2ecc71;"); // Màu xanh lá tích cực

        statusLabel.setText("ĐANG DIỄN RA");
        statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

      } else {
        // TRƯỜNG HỢP 3: Phiên đấu giá đã kết thúc (CLOSED)
        timeLabel.setText("Đã kết thúc");
        timeLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Màu đỏ

        statusLabel.setText("ĐÃ KẾT THÚC");
        statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        countdownTimer.stop(); // Dừng bộ đếm
      }
    }));

    countdownTimer.setCycleCount(Animation.INDEFINITE);
    countdownTimer.play();
  }

  @FXML
  public void gotoAuctionDetail() {
    // Lưu sản phẩm vừa chọn vào SessionManager
    SessionManager.setCurrentAuctionId(auction.getAuctionId());
    SessionManager.setPreviousScreen(currentScreen);
    System.out.println("Đang mở chi tiết phiên đấu giá: " + auction.getAuctionId());
    ScreenController.switchScreen("Bidder/ItemAuction.fxml", "Phiên đấu giá " + auction.getItemName());
  }

  /**
   * Hàm tính toán và định dạng khoảng thời gian còn lại.
   */
  private String formatTimeLeft(LocalDateTime from, LocalDateTime to) {
    long days = ChronoUnit.DAYS.between(from, to);
    long hours = ChronoUnit.HOURS.between(from, to) % 24;
    long minutes = ChronoUnit.MINUTES.between(from, to) % 60;
    long seconds = ChronoUnit.SECONDS.between(from, to) % 60;

    if (days > 0) {
      return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
    } else {
      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
  }

  public String getAuctionId() {
    return auction.getAuctionId();
  }

  public void updatePrice(BigDecimal newPrice) {
    Platform.runLater(() -> {
      priceLabel.setText(String.format("%,.0f VNĐ", newPrice));
    });
  }

}