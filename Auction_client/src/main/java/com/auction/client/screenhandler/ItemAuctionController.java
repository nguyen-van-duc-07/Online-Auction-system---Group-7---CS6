package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.transaction.BidTransaction;
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
import com.auction.shared.network.NetworkConfig;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ItemAuctionController implements Initializable {
  public static ItemAuctionController instance;
  private XYChart.Series<String, Number> priceSeries;
  private int bidSequence = 1;
  @FXML
  private TextField bidAmountField;
  @FXML
  private Label itemNameLabel;
//  @FXML
//  private Label descriptionField;
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

  /** Khung hiển thị ảnh sản phẩm đấu giá (load bất đồng bộ qua HTTP). */
  @FXML
  private ImageView itemImageView;


  // Quick Add buttons
  @FXML
  private Button btnQuickAdd100;
  @FXML
  private Button btnQuickAdd200;
  @FXML
  private Button btnQuickAdd500;
  @FXML
  private Button btnQuickAdd1m;
  @FXML
  private Button btnQuickAdd2m;

  private Timeline countdownTimer;
  private AuctionResponseDTO currentAuction;

  @FXML
  public void handleBack() {
    stopCountdownTimer();
    ServerConnection.sendData(new LeaveRoomRequestDTO(SessionManager.getCurrentAuctionId()));
    ScreenController.goBack();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this; // Gán màn hình hiện tại vào biến static

    // Khóa mặc định 2 ô nhập cấu hình auto-bid khi vừa vào phòng
    if (maxAutoPriceField != null && autoStepPriceField != null) {
      maxAutoPriceField.setDisable(true);
      autoStepPriceField.setDisable(true);
    }

    // Lắng nghe sự kiện bật/tắt checkbox Auto-bid
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
          // --- CẢI TIẾN TRẢI NGHIỆM TẬP TRUNG (FOCUS UX) ---
          // Giật focus ra khỏi ô nhập tay chính (1 lần duy nhất lúc tích)
          bidAmountField.getParent().requestFocus();
          // Tự động đưa con trỏ chuột vào ô "Giá tối đa" để người dùng nhập luôn, đỡ phải click
          Platform.runLater(() -> maxAutoPriceField.requestFocus());
        } else {
          maxAutoPriceField.setDisable(true);
          autoStepPriceField.setDisable(true);
          clearError();

          // Gửi request hủy Bot lên Server
          SetAutoBidRequestDTO req = new SetAutoBidRequestDTO(
              SessionManager.getCurrentUser().getId(),
              SessionManager.getCurrentAuctionId(),
              BigDecimal.ZERO,
              BigDecimal.ZERO,
              false
          );
          ServerConnection.sendData(req);

          Notifications.create()
              .title("Đấu giá tự động")
              .text("Đã tắt chế độ canh giá.")
              .showWarning();
        }

        // GỌI HÀM REFRESH UI CỦA CHÚNG TA Ở ĐÂY
        boolean isActive = currentAuction != null && (currentAuction.getStartTime() == null ||
            (LocalDateTime.now().isAfter(currentAuction.getStartTime()) && LocalDateTime.now().isBefore(currentAuction.getEndTime())));
        updateBidControlState(isActive);

      });
    }
    ServerConnection.sendData(new JoinRoomRequestDTO(SessionManager.getCurrentAuctionId()));
  }

  private void updateBidControlState(boolean isAuctionActive) {
    if (currentAuction == null) return;

    // ÉP JAVAFX REFRESH UI NGAY LẬP TỨC
    Platform.runLater(() -> {
      String currentUserId = SessionManager.getCurrentUser().getId();
      String auctionUserId = currentAuction.getUserId();
      boolean isSeller = currentUserId.equals(auctionUserId);

      if (isSeller || !isAuctionActive) {
        // CHỦ SẢN PHẨM HOẶC PHIÊN ĐÃ ĐÓNG: KHÓA TOÀN TẬP
        bidAmountField.setDisable(true);
        bidAmountField.setEditable(false); // Khóa cấm gõ vật lý
        placeBidButton.setDisable(true);
        if (autoBidCheckBox != null) autoBidCheckBox.setDisable(true);

        bidAmountField.setPromptText(isSeller ? "Chế độ xem (Phiên của bạn)" : "Chưa thể đặt giá lúc này...");
        placeBidButton.setText(isSeller ? "Không thể tự đấu giá" : "Đấu giá (Khóa)");

      } else {
        // NGƯỜI MUA: PHIÊN ĐANG DIỄN RA
        if (autoBidCheckBox != null && autoBidCheckBox.isSelected()) {

          // ================= LỚP KHÓA 3 LỚP =================
          bidAmountField.setDisable(true);      // 1. Làm mờ UI
          bidAmountField.setEditable(false);    // 2. Rút cáp bàn phím (Cấm gõ)
          // ==================================================

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
          // KHÔNG TÍCH AUTO-BID -> XẢ KHÓA
          bidAmountField.setDisable(false);
          bidAmountField.setEditable(true); // Cắm lại cáp bàn phím
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
      // 1. Tự động gỡ dấu tích khỏi ô Checkbox
      if (autoBidCheckBox != null) {
        autoBidCheckBox.setSelected(false);
      }

      // 2. Mở khóa lại 2 ô nhập số để người dùng sẵn sàng tăng ngân sách ("khô máu")
      if (maxAutoPriceField != null) maxAutoPriceField.setDisable(false);
      if (autoStepPriceField != null) autoStepPriceField.setDisable(false);

      // 3. Gọi hàm làm mới cụm nút bấm bên dưới (trả về trạng thái nhập thủ công)
      updateBidControlState(true);

      // 4. KÍCH THÍCH TÂM LÝ: Bắn thông báo cảnh báo màu đỏ (Warning/Error)
      Notifications.create()
          .title("⚠️ Cảnh báo mất vị trí!")
          .text(fomoMessage)
          .hideAfter(javafx.util.Duration.seconds(8)) // Để lâu một chút cho người dùng đọc kịp
          .position(Pos.TOP_RIGHT) // Bắn ở góc trên cùng để đập thẳng vào mắt
          .showError(); // Dùng style Error để tạo cảm giác nguy cấp

      // (Tùy chọn) Focus con trỏ chuột vào ô ngân sách để mời gọi họ nhập số lớn hơn
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
        // TRƯỜNG HỢP 1: CHƯA BẮT ĐẦU (WAITING)
        timeRemainingLabel.setText("Bắt đầu sau: " + formatTimeLeft(now, startTime));
        timeRemainingLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;"); // Màu cam
        updateBidControlState(false); // Khóa form đặt giá

      } else if (startTime == null || now.isAfter(startTime) && now.isBefore(endTime)) {
        // TRƯỜNG HỢP 2: ĐANG DIỄN RA (ACTIVE)
        timeRemainingLabel.setText("Kết thúc sau: " + formatTimeLeft(now, endTime));
        timeRemainingLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // Màu xanh lá
        updateBidControlState(true); // Mở form đặt giá

      } else {
        // TRƯỜNG HỢP 3: ĐÃ KẾT THÚC (CLOSED)
        timeRemainingLabel.setText("Đã kết thúc");
        timeRemainingLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Màu đỏ
        updateBidControlState(false); // Khóa form đặt giá

        // Clear biểu đồ (Giữ lại logic gốc của bạn)
        if (!priceChart.getData().isEmpty()) {
          priceChart.getData().clear();
        }

        if (highestBidderLabel != null && !highestBidderLabel.getText().equals("Phiên đấu giá đã kết thúc")) {
          highestBidderLabel.setText("Phiên đấu giá đã kết thúc");
        }

        countdownTimer.stop(); // Dừng đồng hồ
      }
    }));

    countdownTimer.setCycleCount(Timeline.INDEFINITE);
    countdownTimer.play();
  }

  // CỰC KỲ QUAN TRỌNG: Dừng đồng hồ khi rời khỏi phòng để tránh Memory Leak (Rò rỉ bộ nhớ)
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
      // 2. LOGIC GIỚI HẠN 20 LẦN: Tính toán điểm bắt đầu để lấy 20 bản ghi cuối
      int limit = Math.min(20, history.size());
      List<BidTransaction> recentHistory = new java.util.ArrayList<>(history.subList(0, limit));
      java.util.Collections.reverse(recentHistory);
      for (BidTransaction tx : recentHistory) {
        String formattedTime = tx.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String uniqueXLabel = formattedTime + " (#" + (bidSequence++) + ")";
        priceSeries.getData().add(new XYChart.Data<>(uniqueXLabel, tx.getBidAmount().doubleValue()));
      }
    } else {
      // Kịch bản Sàn đấu giá mới tinh (giữ nguyên logic gốc của bạn)
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

  // Hàm hỗ trợ format text thời gian
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
    // Đổi viền ô nhập thành màu đỏ
    bidAmountField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
  }

  private void clearError() {
    errorLabel.setText("");
    // Đổi viền ô nhập thành màu xanh ngọc đồng bộ hệ thống để báo hiệu thành công
    bidAmountField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
  }

  @FXML
  public void placeBid() {
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

        // Dữ liệu chuẩn -> Gửi Bot lên Server
        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO(
            SessionManager.getCurrentUser().getId(),
            SessionManager.getCurrentAuctionId(),
            maxPrice,
            stepAmount,
            true
        );
        ServerConnection.sendData(req);

        // Khóa ô cấu hình để bot an tâm chạy
        maxAutoPriceField.setDisable(true);
        autoStepPriceField.setDisable(true);
        clearError();

        // Cập nhật lại Text của nút thành "Đang chạy..."
        updateBidControlState(true);

        Notifications.create()
            .title("Đấu giá tự động")
            .text("Hệ thống đang tự động canh giá giúp bạn!")
            .showInformation();

      } catch (NumberFormatException e) {
        showError("Số tiền không hợp lệ. Vui lòng chỉ nhập số!");
      }
      return; // CHẶN LẠI TẠI ĐÂY, KHÔNG CHẠY XUỐNG DƯỚI
    }
    // ================= LUỒNG 2: ĐẤU GIÁ THỦ CÔNG =================
    String bidText = bidAmountField.getText().trim();
    if (bidText.isEmpty()) {
      showError("Vui lòng nhập mức giá bạn muốn đấu!");
      return; // Dừng hàm ngay lập tức
    }
    try {
      BigDecimal bidAmount = new BigDecimal(bidText);
      BigDecimal currentPrice = currentAuction.getCurrentHighestPrice();
      BigDecimal stepPrice = currentAuction.getMinStepPrice();
      System.out.println("[DEBUG PLACEBID] bidAmount=" + bidAmount
          + " | currentPrice=" + currentPrice
          + " | stepPrice=" + stepPrice
          + " | minimum=" + currentPrice.add(stepPrice));
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
      PlaceBidRequestDTO req =
          new PlaceBidRequestDTO(SessionManager.getCurrentAuctionId(),
              SessionManager.getCurrentUser().getId(), SessionManager.getCurrentUser().getAccountName(),
              bidAmount);
      ServerConnection.sendData(req);
      // Xóa ô nhập để người dùng sẵn sàng nhập giá tiếp theo
      bidAmountField.clear();
    } catch (NumberFormatException e) {
      showError("Số tiền không hợp lệ. Vui lòng chỉ nhập số!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Cập nhật text các nút Quick Add dựa vào minStep.
   * - Nút 1: minStep × 1
   * - Nút 2: minStep × 1.25
   * - Nút 3: minStep × 1.5
   * - Nút 4: minStep × 1.75
   * - Nút 5: minStep × 2
   */
  private void updateQuickAddButtonTexts() {
    if (currentAuction == null || currentAuction.getMinStepPrice() == null) {
      return;
    }

    BigDecimal minStep = currentAuction.getMinStepPrice();
    DecimalFormat formatter = new DecimalFormat("#,###");

    // Định nghĩa các hệ số nhân
    double[] multipliers = {1.0, 1.25, 1.5, 1.75, 2.0};
    Button[] buttons = {btnQuickAdd100, btnQuickAdd200, btnQuickAdd500, btnQuickAdd1m, btnQuickAdd2m};

    for (int i = 0; i < buttons.length && i < multipliers.length; i++) {
      BigDecimal amount = minStep.multiply(new BigDecimal(multipliers[i]));
      String formattedAmount = formatAmountForButton(amount);
      buttons[i].setText(formattedAmount);
    }
  }

  /**
   * Format lượng tiền để hiển thị trên nút (ví dụ: 100k, 1.5M, v.v.)
   */
  private String formatAmountForButton(BigDecimal amount) {
    if (amount.compareTo(new BigDecimal("1000000")) >= 0) {
      // Triệu (M)
      BigDecimal millions = amount.divide(new BigDecimal("1000000"), 2, java.math.RoundingMode.HALF_UP);
      String result = millions.stripTrailingZeros().toPlainString();
      return result + "M";
    } else if (amount.compareTo(new BigDecimal("1000")) >= 0) {
      // Nghìn (k)
      BigDecimal thousands = amount.divide(new BigDecimal("1000"), 1, java.math.RoundingMode.HALF_UP);
      String result = thousands.stripTrailingZeros().toPlainString();
      return result + "k";
    } else {
      return amount.stripTrailingZeros().toPlainString();
    }
  }

  /**
   * Xử lý nút Quick Add: tính toán giá mới dựa trên Min Step hoặc phần trăm.
   * - "Min Step": cộng minStepPrice
   * - "+125%", "+150%", "+175%", "+200%": cộng % của giá hiện tại
   */
  @FXML
  public void handleQuickAdd(ActionEvent event) {
    // Phòng thủ: Nếu thông tin đấu giá chưa được tải
    if (currentAuction == null) {
      return;
    }
    // Nếu ô nhập giá đang bị khóa (ví dụ: phiên đã kết thúc hoặc đang bật auto-bid) thì không cho chọn nhanh
    if (bidAmountField.isDisabled() || !bidAmountField.isEditable()) {
      return;
    }
    if (event.getSource() instanceof Button) {
      Button clickedButton = (Button) event.getSource();
      String text = clickedButton.getText().trim().toLowerCase();

      BigDecimal currentPrice = currentAuction.getCurrentHighestPrice();
      if (currentPrice == null) {
        showError("Không thể lấy giá hiện tại!");
        return;
      }

      BigDecimal addAmount = BigDecimal.ZERO;
      try {
        // Kiểm tra loại nút
        if (text.equals("min step")) {
          // Nút Min Step: cộng minStepPrice
          BigDecimal minStep = currentAuction.getMinStepPrice();
          if (minStep == null || minStep.compareTo(BigDecimal.ZERO) <= 0) {
            showError("Bước giá tối thiểu không hợp lệ!");
            return;
          }
          addAmount = minStep;
        } else if (text.contains("%")) {
          // Nút phần trăm (ví dụ: "+125%")
          String numStr = text.replace("+", "").replace("%", "").trim();
          BigDecimal percentage = new BigDecimal(numStr);
          // Cộng giá hiện tại với (currentPrice * percentage%)
          addAmount = currentPrice.multiply(percentage).divide(new BigDecimal("100"));
        } else {
          // Trường hợp cũ: xử lý hậu tố "k" hoặc "m"
          if (text.startsWith("+")) {
            text = text.substring(1);
          }
          if (text.endsWith("k")) {
            String numStr = text.substring(0, text.length() - 1);
            addAmount = new BigDecimal(numStr).multiply(new BigDecimal("1000"));
          } else if (text.endsWith("m")) {
            String numStr = text.substring(0, text.length() - 1);
            addAmount = new BigDecimal(numStr).multiply(new BigDecimal("1000000"));
          } else {
            addAmount = new BigDecimal(text);
          }
        }

        // Tính toán giá mới
        BigDecimal newBidAmount = currentPrice.add(addAmount);

        // Điền giá trị mới vào ô nhập liệu
        bidAmountField.setText(newBidAmount.toPlainString());

        // Xóa các thông báo lỗi cũ
        clearError();
      } catch (NumberFormatException e) {
        showError("Tính toán giá không hợp lệ!");
      } catch (Exception e) {
        showError("Lỗi: " + e.getMessage());
      }
    }
  }

  public void showBidSuccess(String itemName, BigDecimal amount) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Đấu giá thành công");
    alert.setHeaderText("Hệ thống đã ghi nhận giá đặt của bạn!");

    // Format lại số tiền cho đẹp (ví dụ: 42,000,000 VNĐ)
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

    // Cập nhật text cho Label
    currentPriceField.setText("Giá hiện tại: " + formattedPrice);

    // (Tùy chọn UX) Nháy màu để người dùng chú ý giá vừa thay đổi
    // 1. Lưu lại style gốc (màu cam) để tí nữa quay về
    String originalStyle = "-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 19";

    // 2. Đổi sang màu xanh để báo hiệu thành công
    currentPriceField.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 23");

    // 3. Tạo một độ trễ 1 giây, sau đó tự động trả về màu cũ
    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
    pause.setOnFinished(e -> currentPriceField.setStyle(originalStyle));
    pause.play();
  }

  public void onNewBidReceived(NewBidDTO newBid) {
    Platform.runLater(() -> {
      // 1. Cập nhật giá mới nhất vào biến hiện tại
      currentAuction.setCurrentHighestPrice(newBid.getBidAmount());
      updateHighestBidderUI(newBid.getBidderName());
      // 2. Chạy hiệu ứng nháy màu và cập nhật Text
      refreshPriceUI();

      // 3. Thêm điểm ảnh mới vào biểu đồ LineChart/AreaChart
      if (priceSeries != null) {
        // Lấy thời gian thực lúc nhận được Bid
        String currentTime = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        String uniqueXLabel = currentTime + " (#" + (bidSequence++) + ")";
        // Thêm điểm mới vào đường series hiện tại
        // Lưu ý: Dùng doubleValue() vì Chart yêu cầu kiểu Number
        priceSeries.getData().add(new XYChart.Data<>(uniqueXLabel, newBid.getBidAmount().doubleValue()));

        // (Tùy chọn) Giới hạn số điểm hiển thị để biểu đồ đẹp, không bị quá dày
        if (priceSeries.getData().size() > 20) {
          priceSeries.getData().remove(0);
        }
      }
      // 4. Phân loại thông báo (UX/HMI)
      String myUserId = SessionManager.getCurrentUser().getId();
      if (!newBid.getBidderId().equals(myUserId)) {
        // Trường hợp 1: Người KHÁC vừa đặt giá -> Báo góc màn hình
        Notifications.create()
            .title("🔥 Có người vừa trả giá!")
            .text("Người chơi " + newBid.getBidderName() + " vừa đặt giá mới")
            .hideAfter(javafx.util.Duration.seconds(5)) // Tự biến mất sau 5 giây
            .position(Pos.BOTTOM_RIGHT) // Hiện ở góc dưới bên phải màn hình
            .threshold(3, Notifications.create().title("Nhiều thông báo quá!")) // Giới hạn nếu nổ bid liên tục
            .showInformation(); // Hoặc .showWarning() nếu bạn muốn đổi icon
      } else {
        // Trường hợp 2: LÀ MÌNH vừa đặt giá thành công
        // Phòng thủ: Cố gắng lấy tên từ currentAuction
        String itemName = "sản phẩm này"; // Tên mặc định nếu mọi cách đều thất bại
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

  // Xử lý khi nhận được toàn bộ lịch sử đấu giá lúc vừa vào phòng
  public void onAuctionRoomJoined(AuctionResponseDTO auctionData) {
    Platform.runLater(() -> {
      // 1. Cập nhật lại đối tượng đấu giá hiện tại với đầy đủ lịch sử từ Server
      this.currentAuction = auctionData;
      // Cập nhật text các nút Quick Add dựa vào minStep
      updateQuickAddButtonTexts();

      itemNameLabel.setText(auctionData.getItem().getName());

      DecimalFormat formatter = new DecimalFormat("#,###");
      String formattedMinStepPrice = formatter.format(auctionData.getMinStepPrice());
      minStepPriceLabel.setText("Bước giá quy định tối thiểu: " + formattedMinStepPrice + " VNĐ");

      // 2. Cập nhật UI cơ bản: Tên người cao nhất và Giá hiện tại
      updateHighestBidderUI(auctionData.getHighestBidderName());

      String formattedPrice = formatter.format(auctionData.getCurrentHighestPrice()) + " VNĐ";
      currentPriceField.setText("Giá hiện tại: " + formattedPrice);

      // 3. Bắt đầu khởi động bộ đếm thời gian
      startCountdownTimer();

      // 4. Vẽ lại biểu đồ dựa trên dữ liệu lịch sử mới nhất
      loadChartData();

      // 5. Load ảnh sản phẩm bất đồng bộ qua HTTP
      if (auctionData.getImagePath() != null && !auctionData.getImagePath().isEmpty()
          && itemImageView != null) {
        String imageUrl = "http://" + NetworkConfig.DEFAULT_HOST + ":"
            + NetworkConfig.IMAGE_SERVER_PORT + "/images/" + auctionData.getImagePath();
        Image image = new Image(imageUrl, true); // true = background loading
        itemImageView.setImage(image);
      }

      // In log ra để dễ debug
      int historySize = (auctionData.getBidHistory() != null) ? auctionData.getBidHistory().size() : 0;
      // THÊM
      System.out.println("[DEBUG JOIN] currentHighestPrice=" + auctionData.getCurrentHighestPrice()
          + " | minStepPrice=" + auctionData.getMinStepPrice());
      System.out.println(">>> Đã đồng bộ thành công lịch sử đấu giá: " + historySize + " bản ghi.");
    });
  }

  public void onAuctionExtended(LocalDateTime newEndTime) {
    Platform.runLater(() -> {
      currentAuction.setEndTime(newEndTime);

      Notifications.create()
          .title("⏰ Phiên đấu giá được gia hạn!")
          .text("Có bid mới trong 3 phút cuối!\nPhiên được gia hạn thêm 3 phút.")
          .hideAfter(javafx.util.Duration.seconds(5))
          .position(Pos.TOP_RIGHT)
          .showWarning();
    });
  }
}
