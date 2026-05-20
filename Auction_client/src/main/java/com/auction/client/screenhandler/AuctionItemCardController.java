package com.auction.client.screenhandler;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.enums.AuctionStatusUI;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.response.AuctionResponseDTO;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
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
  @FXML
  private Button bidButton;
  private Timeline countdownTimer;
  private String auctionId;

  /**
   * Hàm này dùng để bơm dữ liệu từ một Controller cha truyền sang.
   * Controller cha phải implement ProductDetailNavigator.
   */
  public void setData(AuctionDTO auction, ProductDetailNavigator navigator) {
    this.auctionId = auction.getAuctionId();
    // 1. Đổ dữ liệu text
    nameLabel.setText(auction.getItemName());

    updateStatus(auction.getStartTime(), auction.getEndTime());

    String formattedPrice = String.format("%,.0f VNĐ", auction.getCurrentPrice());
    priceLabel.setText(formattedPrice);

    // Tạo bộ đếm thời gian chạy mỗi 1 giây
    countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e ->
        updateStatus(auction.getStartTime(), auction.getEndTime())
    ));

    countdownTimer.setCycleCount(Animation.INDEFINITE);
    countdownTimer.play();

    // 2. Gắn sự kiện cho nút bấm
    // Gọi ngược lại hàm chuyển trang của navigator
    bidButton.setOnAction(e -> navigator.gotoProductDetail(auction));
  }

  /**
   * Hàm tính toán và định dạng khoảng thời gian còn lại
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
    return auctionId;
  }

  public void updatePrice(BigDecimal newPrice) {
    Platform.runLater(() -> {
      priceLabel.setText(String.format("%,.0f VNĐ", newPrice));
    });
  }
  private void updateStatus(LocalDateTime startTime, LocalDateTime endTime) {
    LocalDateTime now = LocalDateTime.now();

    if (startTime != null && now.isBefore(startTime)) {
      timeLabel.setText(formatTimeLeft(now, startTime));
      timeLabel.setStyle("-fx-text-fill: #f39c12;");
      statusLabel.setText("SẮP DIỄN RA");
      statusLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");

    } else if (endTime != null && now.isBefore(endTime)) {
      timeLabel.setText(formatTimeLeft(now, endTime));
      timeLabel.setStyle("-fx-text-fill: #2ecc71;");
      statusLabel.setText("ĐANG DIỄN RA");
      statusLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");

    } else {
      timeLabel.setText("Đã kết thúc");
      timeLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
      statusLabel.setText("ĐÃ KẾT THÚC");
      statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
      if (countdownTimer != null) countdownTimer.stop();
    }
  }
}