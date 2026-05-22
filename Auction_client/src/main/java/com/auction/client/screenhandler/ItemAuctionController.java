package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.JoinRoomRequestDTO;
import com.auction.shared.request.LeaveRoomRequestDTO;
import com.auction.shared.request.PlaceBidRequestDTO;
import com.auction.shared.request.SetAutoBidRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.response.NewBidDTO;
import com.auction.shared.response.PlaceBidResponseDTO;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.text.DecimalFormat;
import org.controlsfx.control.Notifications;

public class ItemAuctionController implements Initializable {
  public static ItemAuctionController instance;
  private XYChart.Series<String, Number> priceSeries;
  private int bidSequence = 1;
  @FXML
  private TextField bidAmountField;
  @FXML
  private Label itemNameLabel;
  @FXML
  private Label currentPriceField;
  @FXML
  private AreaChart<String, Number> priceChart;
  @FXML
  private Label errorLabel;
  @FXML
  private Label timeRemainingLabel;
  @FXML
  private Button placeBidButton;
  @FXML
  private Label highestBidderLabel;
  @FXML
  private Label minStepPriceLabel;

  // --- AUTO-BID UI COMPONENTS ---
  @FXML
  private CheckBox autoBidCheckBox;
  @FXML
  private TextField maxAutoPriceField;
  @FXML
  private TextField autoStepPriceField;

  private Timeline countdownTimer;
  private AuctionResponseDTO currentAuction;

  @FXML
  public void gotoHome() {
    stopCountdownTimer();
    ServerConnection.sendData(new LeaveRoomRequestDTO(SessionManager.getCurrentAuctionId()));
    ScreenController.goBack();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;

    if (maxAutoPriceField != null && autoStepPriceField != null) {
      maxAutoPriceField.setDisable(true);
      autoStepPriceField.setDisable(true);
    }

    if (autoBidCheckBox != null) {
      autoBidCheckBox.setOnAction(event -> {

        if (autoBidCheckBox.isSelected()) {
          maxAutoPriceField.setDisable(false);
          autoStepPriceField.setDisable(false);

          if (currentAuction != null && autoStepPriceField.getText().isEmpty()) {
            autoStepPriceField.setText(currentAuction.getMinStepPrice().toString());
          }
          bidAmountField.clear();
          clearError();
          bidAmountField.getParent().requestFocus();
          Platform.runLater(() -> maxAutoPriceField.requestFocus());
        } else {
          maxAutoPriceField.setDisable(true);
          autoStepPriceField.setDisable(true);
          clearError();

          // FIX 1: Bẫy lỗi khi tắt Auto-bid
          UserDTO currentUser = SessionManager.getCurrentUser();
          if (currentUser != null) {
            SetAutoBidRequestDTO req = new SetAutoBidRequestDTO(
                    currentUser.getId(),
                    SessionManager.getCurrentAuctionId(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    false
            );
            ServerConnection.sendData(req);
          } else {
            System.err.println(">>> [BÁO ĐỘNG] Mất session người dùng khi tắt Auto-bid!");
          }

          Notifications.create()
                  .title("Đấu giá tự động")
                  .text("Đã tắt chế độ canh giá.")
                  .showWarning();
        }

        boolean isActive = currentAuction != null && (currentAuction.getStartTime() == null ||
                (LocalDateTime.now().isAfter(currentAuction.getStartTime()) && LocalDateTime.now().isBefore(currentAuction.getEndTime())));
        updateBidControlState(isActive);

      });
    }
    ServerConnection.sendData(new JoinRoomRequestDTO(SessionManager.getCurrentAuctionId()));
  }

  private void updateBidControlState(boolean isAuctionActive) {
    if (currentAuction == null) return;

    Platform.runLater(() -> {
      // FIX 2: Bẫy lỗi vòng lặp cập nhật UI
      UserDTO currentUser = SessionManager.getCurrentUser();

      if (currentUser == null) {
        bidAmountField.setDisable(true);
        placeBidButton.setDisable(true);
        if (autoBidCheckBox != null) autoBidCheckBox.setDisable(true);
        bidAmountField.setPromptText("Lỗi: Chưa đăng nhập!");
        System.err.println(">>> [BÁO ĐỘNG] SessionManager.getCurrentUser() đang bị NULL trong hàm updateBidControlState!");
        return;
      }

      String currentUserId = currentUser.getId();
      String auctionUserId = currentAuction.getUserId();
      boolean isSeller = currentUserId.equals(auctionUserId);

      if (isSeller || !isAuctionActive) {
        bidAmountField.setDisable(true);
        bidAmountField.setEditable(false);
        placeBidButton.setDisable(true);
        if (autoBidCheckBox != null) autoBidCheckBox.setDisable(true);

        bidAmountField.setPromptText(isSeller ? "Chế độ xem (Phiên của bạn)" : "Chưa thể đặt giá lúc này...");
        placeBidButton.setText(isSeller ? "Không thể tự đấu giá" : "Đấu giá (Khóa)");

      } else {
        if (autoBidCheckBox != null && autoBidCheckBox.isSelected()) {
          bidAmountField.setDisable(true);
          bidAmountField.setEditable(false);

          if (maxAutoPriceField.isDisabled()) {
            bidAmountField.setPromptText("Đang chạy tự động...");
            placeBidButton.setDisable(true);
            placeBidButton.setText("Đang chạy Auto-bid");
          } else {
            bidAmountField.setPromptText("Nhập giá tối đa ở dưới...");
            placeBidButton.setDisable(false);
            placeBidButton.setText("Xác nhận Auto-bid");
          }

        } else {
          bidAmountField.setDisable(false);
          bidAmountField.setEditable(true);
          placeBidButton.setDisable(false);
          if (autoBidCheckBox != null) autoBidCheckBox.setDisable(false);

          bidAmountField.setPromptText("Nhập mức giá...");
          placeBidButton.setText("Đấu giá");
        }
      }
    });
  }

  public void onAutoBidDefeated(String fomoMessage) {
    Platform.runLater(() -> {
      if (autoBidCheckBox != null) {
        autoBidCheckBox.setSelected(false);
      }
      if (maxAutoPriceField != null) maxAutoPriceField.setDisable(false);
      if (autoStepPriceField != null) autoStepPriceField.setDisable(false);

      updateBidControlState(true);

      Notifications.create()
              .title("⚠️ Cảnh báo mất vị trí!")
              .text(fomoMessage)
              .hideAfter(javafx.util.Duration.seconds(8))
              .position(Pos.TOP_RIGHT)
              .showError();

      maxAutoPriceField.requestFocus();
    });
  }

  private void updateHighestBidderUI(String name) {
    if (name == null || name.isEmpty()) {
      highestBidderLabel.setText("Chưa có");
    } else {
      highestBidderLabel.setText(name);
    }
  }

  private void startCountdownTimer() {
    countdownTimer = new Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime startTime = currentAuction.getStartTime();
      LocalDateTime endTime = currentAuction.getEndTime();

      if (startTime != null && now.isBefore(startTime)) {
        timeRemainingLabel.setText("Bắt đầu sau: " + formatTimeLeft(now, startTime));
        timeRemainingLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        updateBidControlState(false);

      } else if (startTime == null || now.isAfter(startTime) && now.isBefore(endTime)) {
        timeRemainingLabel.setText("Kết thúc sau: " + formatTimeLeft(now, endTime));
        timeRemainingLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        updateBidControlState(true);

      } else {
        timeRemainingLabel.setText("Đã kết thúc");
        timeRemainingLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        updateBidControlState(false);

        if (!priceChart.getData().isEmpty()) {
          priceChart.getData().clear();
        }

        if (highestBidderLabel != null && !highestBidderLabel.getText().equals("Phiên đấu giá đã kết thúc")) {
          highestBidderLabel.setText("Phiên đấu giá đã kết thúc");
        }

        countdownTimer.stop();
      }
    }));

    countdownTimer.setCycleCount(Timeline.INDEFINITE);
    countdownTimer.play();
  }

  public void stopCountdownTimer() {
    if (countdownTimer != null) {
      countdownTimer.stop();
    }
    ServerConnection.sendData(new LeaveRoomRequestDTO(SessionManager.getCurrentAuctionId()));
  }

  private void loadChartData() {
    javafx.scene.chart.NumberAxis yAxis = (javafx.scene.chart.NumberAxis) priceChart.getYAxis();
    yAxis.setForceZeroInRange(false);
    javafx.scene.chart.CategoryAxis xAxis = (javafx.scene.chart.CategoryAxis) priceChart.getXAxis();
    xAxis.setAnimated(false);
    xAxis.getCategories().clear();
    xAxis.setTickLabelRotation(-90);
    xAxis.setTickLabelsVisible(true);
    priceSeries = new XYChart.Series<>();
    priceSeries.setName("Lịch sử giá");
    priceChart.getData().clear();
    bidSequence = 1;
    List<BidTransaction> history = currentAuction.getBidHistory();

    if (history != null && !history.isEmpty()) {
      int limit = Math.min(20, history.size());
      List<BidTransaction> recentHistory = new java.util.ArrayList<>(history.subList(0, limit));
      java.util.Collections.reverse(recentHistory);
      for (BidTransaction tx : recentHistory) {
        String formattedTime = tx.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String uniqueXLabel = formattedTime + " (#" + (bidSequence++) + ")";
        priceSeries.getData().add(new XYChart.Data<>(uniqueXLabel, tx.getBidAmount().doubleValue()));
      }
    } else {
      priceSeries.getData().add(new XYChart.Data<>("Bắt đầu", currentAuction.getCurrentHighestPrice().doubleValue()));

      Notifications.create()
              .title("Sàn đấu giá đã mở!")
              .text("Sản phẩm chưa có ai trả giá. Hãy là người dẫn đầu!")
              .hideAfter(javafx.util.Duration.seconds(8))
              .position(Pos.BOTTOM_RIGHT)
              .showInformation();
    }

    priceChart.getData().add(priceSeries);
  }

  private String formatTimeLeft(LocalDateTime from, LocalDateTime to) {
    long days = java.time.temporal.ChronoUnit.DAYS.between(from, to);
    long hours = java.time.temporal.ChronoUnit.HOURS.between(from, to) % 24;
    long minutes = java.time.temporal.ChronoUnit.MINUTES.between(from, to) % 60;
    long seconds = java.time.temporal.ChronoUnit.SECONDS.between(from, to) % 60;

    if (days > 0) {
      return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
    } else {
      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
  }

  private void showError(String message) {
    errorLabel.setText(message);
    bidAmountField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
  }

  private void clearError() {
    errorLabel.setText("");
    bidAmountField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
  }

  @FXML
  public void placeBid() {
    // FIX 3: Bẫy lỗi mất session khi bấm nút đặt giá
    UserDTO currentUser = SessionManager.getCurrentUser();
    if (currentUser == null) {
      showError("Lỗi hệ thống: Mất phiên đăng nhập. Vui lòng thử lại!");
      System.err.println(">>> [BÁO ĐỘNG] Mất session khi thực hiện placeBid()");
      return;
    }

    // ================= LUỒNG 1: XÁC NHẬN AUTO-BID =================
    if (autoBidCheckBox != null && autoBidCheckBox.isSelected()) {
      String maxText = maxAutoPriceField.getText().trim();
      String stepText = autoStepPriceField.getText().trim();

      if (maxText.isEmpty() || stepText.isEmpty()) {
        showError("Vui lòng nhập giá tối đa và bước giá!");
        return;
      }
      try {
        BigDecimal maxPrice = new BigDecimal(maxText);
        BigDecimal stepAmount = new BigDecimal(stepText);

        if (maxPrice.compareTo(currentAuction.getCurrentHighestPrice()) <= 0) {
          showError("Giá tối đa phải lớn hơn giá hiện tại!");
          return;
        }

        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO(
                currentUser.getId(), // Đã thay thế để lấy an toàn
                SessionManager.getCurrentAuctionId(),
                maxPrice,
                stepAmount,
                true
        );
        ServerConnection.sendData(req);

        maxAutoPriceField.setDisable(true);
        autoStepPriceField.setDisable(true);
        clearError();

        updateBidControlState(true);

        Notifications.create()
                .title("Đấu giá tự động")
                .text("Hệ thống đang tự động canh giá giúp bạn!")
                .showInformation();

      } catch (NumberFormatException e) {
        showError("Số tiền không hợp lệ. Vui lòng chỉ nhập số!");
      }
      return;
    }

    // ================= LUỒNG 2: ĐẤU GIÁ THỦ CÔNG =================
    String bidText = bidAmountField.getText().trim();
    if (bidText.isEmpty()) {
      showError("Vui lòng nhập mức giá bạn muốn đấu!");
      return;
    }
    try {
      BigDecimal bidAmount = new BigDecimal(bidText);
      BigDecimal currentPrice = currentAuction.getCurrentHighestPrice();
      BigDecimal stepPrice = currentAuction.getMinStepPrice();

      if (bidAmount.compareTo(currentPrice) <= 0) {
        showError("Mức giá phải lớn hơn giá hiện tại của sản phẩm!");
        return;
      }
      if (bidAmount.compareTo(currentPrice.add(stepPrice)) < 0) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        showError("Mức giá phải cao hơn ít nhất 1 bước giá (" + formatter.format(stepPrice) + " VNĐ)!");
        return;
      }

      clearError();
      PlaceBidRequestDTO req = new PlaceBidRequestDTO(
              SessionManager.getCurrentAuctionId(),
              currentUser.getId(), // Đã thay thế để lấy an toàn
              currentUser.getRealName(), // Đã thay thế để lấy an toàn
              bidAmount
      );
      ServerConnection.sendData(req);
      bidAmountField.clear();

    } catch (NumberFormatException e) {
      showError("Số tiền không hợp lệ. Vui lòng chỉ nhập số!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void handleQuickAdd(ActionEvent event) {
    if (currentAuction == null) {
      return;
    }
    if (bidAmountField.isDisabled() || !bidAmountField.isEditable()) {
      return;
    }
    if (event.getSource() instanceof Button) {
      Button clickedButton = (Button) event.getSource();
      String text = clickedButton.getText().trim().toLowerCase();

      if (text.startsWith("+")) {
        text = text.substring(1);
      }
      BigDecimal addAmount = BigDecimal.ZERO;
      try {
        if (text.endsWith("k")) {
          String numStr = text.substring(0, text.length() - 1);
          addAmount = new BigDecimal(numStr).multiply(new BigDecimal("1000"));
        } else if (text.endsWith("m")) {
          String numStr = text.substring(0, text.length() - 1);
          addAmount = new BigDecimal(numStr).multiply(new BigDecimal("1000000"));
        } else {
          addAmount = new BigDecimal(text);
        }

        BigDecimal currentPrice = currentAuction.getCurrentHighestPrice();
        if (currentPrice != null) {
          BigDecimal newBidAmount = currentPrice.add(addAmount);
          bidAmountField.setText(newBidAmount.toPlainString());
          clearError();
        }
      } catch (NumberFormatException e) {
        showError("Lượng tiền cộng thêm không hợp lệ!");
      }
    }
  }

  public void showBidSuccess(String itemName, BigDecimal amount) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Đấu giá thành công");
    alert.setHeaderText("Hệ thống đã ghi nhận giá đặt của bạn!");

    DecimalFormat formatter = new DecimalFormat("#,###");
    String formattedAmount = formatter.format(amount) + " VNĐ";

    alert.setContentText("Chúc mừng! Bạn đã đặt giá " + formattedAmount +
            " cho sản phẩm: " + itemName + ".\n\n" +
            "Hãy theo dõi phiên đấu giá để cập nhật tình hình nhé.");

    alert.showAndWait();
  }

  private void refreshPriceUI() {
    DecimalFormat formatter = new DecimalFormat("#,###");
    String formattedPrice = formatter.format(currentAuction.getCurrentHighestPrice()) + " VNĐ";

    currentPriceField.setText("Giá hiện tại: " + formattedPrice);

    String originalStyle = "-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 19";
    currentPriceField.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 23");

    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
    pause.setOnFinished(e -> currentPriceField.setStyle(originalStyle));
    pause.play();
  }

  public void onNewBidReceived(NewBidDTO newBid) {
    Platform.runLater(() -> {
      currentAuction.setCurrentHighestPrice(newBid.getBidAmount());
      updateHighestBidderUI(newBid.getBidderName());
      refreshPriceUI();

      if (priceSeries != null) {
        String currentTime = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        String uniqueXLabel = currentTime + " (#" + (bidSequence++) + ")";
        priceSeries.getData().add(new XYChart.Data<>(uniqueXLabel, newBid.getBidAmount().doubleValue()));

        if (priceSeries.getData().size() > 20) {
          priceSeries.getData().remove(0);
        }
      }

      // FIX 4: So sánh ID an toàn khi có tin nhắn mới
      UserDTO currentUser = SessionManager.getCurrentUser();
      String myUserId = (currentUser != null) ? currentUser.getId() : "";

      if (!newBid.getBidderId().equals(myUserId)) {
        Notifications.create()
                .title("🔥 Có người vừa trả giá!")
                .text("Người chơi " + newBid.getBidderName() + " vừa đặt giá mới")
                .hideAfter(javafx.util.Duration.seconds(5))
                .position(Pos.BOTTOM_RIGHT)
                .threshold(3, Notifications.create().title("Nhiều thông báo quá!"))
                .showInformation();
      } else {
        String itemName = "sản phẩm này";
        if (currentAuction.getItem() != null) {
          itemName = currentAuction.getItem().getName();
        }
        showBidSuccess(itemName, newBid.getBidAmount());
      }
    });
  }

  public void onPlaceBidResponse(PlaceBidResponseDTO response) {
    Platform.runLater(() -> {
      if (!response.isSuccess()) {
        showError(response.getMessage());
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Không thể trả giá");
        alert.setHeaderText(null);
        alert.setContentText(response.getMessage());
        alert.showAndWait();
      } else {
        clearError();
      }
    });
  }

  public void onAuctionRoomJoined(AuctionResponseDTO auctionData) {
    Platform.runLater(() -> {
      this.currentAuction = auctionData;
      itemNameLabel.setText(auctionData.getItem().getName());

      DecimalFormat formatter = new DecimalFormat("#,###");
      String formattedMinStepPrice = formatter.format(auctionData.getMinStepPrice());
      minStepPriceLabel.setText("Bước giá quy định tối thiểu: " + formattedMinStepPrice + " VNĐ");

      updateHighestBidderUI(auctionData.getHighestBidderName());

      String formattedPrice = formatter.format(auctionData.getCurrentHighestPrice()) + " VNĐ";
      currentPriceField.setText("Giá hiện tại: " + formattedPrice);

      startCountdownTimer();
      loadChartData();

      int historySize = (auctionData.getBidHistory() != null) ? auctionData.getBidHistory().size() : 0;
      System.out.println(">>> Đã đồng bộ thành công lịch sử đấu giá: " + historySize + " bản ghi.");
    });
  }

  @FXML
  public void handleBack() {
    ScreenController.goBack();
  }
}