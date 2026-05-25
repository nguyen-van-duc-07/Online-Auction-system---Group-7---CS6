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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionItemCardController {
  private static final Logger log = LoggerFactory.getLogger(AuctionItemCardController.class);

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

      updateStatus(auction.getStartTime(), auction.getEndTime());

      String formattedPrice = String.format("%,.0f VNĐ", auction.getCurrentPrice());
      priceLabel.setText(formattedPrice);

      // Tạo bộ đếm thời gian chạy mỗi 1 giây
      countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e ->
          updateStatus(auction.getStartTime(), auction.getEndTime())
      ));

      countdownTimer.setCycleCount(Animation.INDEFINITE);
      countdownTimer.play();
  }

  @FXML
  public void gotoAuctionDetail() {
    // Lưu sản phẩm vừa chọn vào SessionManager
    SessionManager.setCurrentAuctionId(auction.getAuctionId());
    SessionManager.setPreviousScreen(currentScreen);
    log.info("Đang mở chi tiết phiên đấu giá: {}", auction.getAuctionId());
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
  public void updateEndTime(LocalDateTime newEndTime) {
    this.auction.setEndTime(newEndTime);
  }
}