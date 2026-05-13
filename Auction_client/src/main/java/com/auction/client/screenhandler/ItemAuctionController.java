package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.request.JoinRoomRequestDTO;
import com.auction.shared.request.LeaveRoomRequestDTO;
import com.auction.shared.request.PlaceBidRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.text.DecimalFormat;

public class ItemAuctionController implements Initializable {
  @FXML
  private TextField bidAmountField;
  @FXML
  private Label itemNameLabel;
  @FXML
  private Label descriptionField;
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

  private Timeline countdownTimer;
  private AuctionResponseDTO currentAuction;
  private HomeController homeController = new HomeController();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.currentAuction = SessionManager.getCurrentAuction();
    ServerConnection.sendData(new JoinRoomRequestDTO(SessionManager.getCurrentAuction().getId()));
    itemNameLabel.setText(SessionManager.getCurrentAuction().getItem().getName());
    descriptionField.setText(SessionManager.getCurrentAuction().getItem().getDescription());
    DecimalFormat formatter = new DecimalFormat("#,###");
    String formattedPrice = formatter.format(SessionManager.getCurrentAuction().getCurrentHighestPrice()) + " VNĐ";
    currentPriceField.setText("Giá hiện tại: " +
        formattedPrice);
    loadChartData();
    startCountdownTimer();
  }

  private void startCountdownTimer() {
    LocalDateTime endTime = currentAuction.getEndTime();

    // Gọi Helper. Nếu hết giờ thì truyền lệnh ẩn nút "Đấu giá" vào (Chuẩn UX)
    countdownTimer = CountdownHelper.setupCountdown(timeRemainingLabel, endTime, () -> {
      placeBidButton.setDisable(true); // Khóa nút khi hết giờ
      countdownTimer.stop();      // Dừng đồng hồ
    });

    countdownTimer.play();
  }

  // CỰC KỲ QUAN TRỌNG: Dừng đồng hồ khi rời khỏi phòng để tránh Memory Leak (Rò rỉ bộ nhớ)
  public void stopCountdownTimer() {
    if (countdownTimer != null) {
      countdownTimer.stop();
    }
    ServerConnection.sendData(new LeaveRoomRequestDTO(SessionManager.getCurrentAuction().getId()));
  }

  private void loadChartData() {
    // 1. Tạo một "Series" (một đường biểu diễn dữ liệu)
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Lịch sử giá"); // Tên của đường (có thể ẩn đi nếu không cần)

    // 2. Thêm các điểm dữ liệu (Trục X là Thời gian (String), Trục Y là Giá (Number))
    // Trong thực tế, bạn sẽ dùng vòng lặp để lấy dữ liệu từ Database (MySQL/SQL Server) ở đây
    series.getData().add(new XYChart.Data<>("10:00", 10.0));
    series.getData().add(new XYChart.Data<>("10:15", 12.5));
    series.getData().add(new XYChart.Data<>("10:30", 15.2));
    series.getData().add(new XYChart.Data<>("11:00", 20.0));
    series.getData().add(new XYChart.Data<>("11:20", 25.5)); // Mức giá cao nhất hiện tại

    // 3. Xóa dữ liệu cũ (nếu có) và đưa Series mới vào biểu đồ
    priceChart.getData().clear();
    priceChart.getData().add(series);
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
  public void gotoResult() {
    leaveAuctionRoom();
    homeController.gotoResult();
  }

  @FXML
  public void gotoProfile() {
    leaveAuctionRoom();
    homeController.gotoProfile();
  }

  @FXML
  public void gotoLogin() {
    leaveAuctionRoom();
    homeController.gotoLogin();
  }

  @FXML
  public void gotoWallet() {
    leaveAuctionRoom();
    homeController.gotoWallet();
  }

  @FXML
  public void gotoHomeWithHyperLink() {
    leaveAuctionRoom();
    ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
  }

  @FXML
  public void gotoSellerHome() {
    leaveAuctionRoom();
    homeController.gotoSellerHome();
  }

  @FXML
  public void placeBid() {
    String bidText = bidAmountField.getText().trim();
    if (bidText.isEmpty()) {
      showError("Vui lòng nhập mức giá bạn muốn đấu!");
      return; // Dừng hàm ngay lập tức
    }
    try{
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
      PlaceBidRequestDTO req =
              new PlaceBidRequestDTO(SessionManager.getCurrentAuction().getId(),
                      SessionManager.getCurrentUser().getId(),
                      bidAmount);
      ServerConnection.sendData(req);
      currentAuction.setCurrentHighestPrice(bidAmount);
      refreshPriceUI();
      // Xóa ô nhập để người dùng sẵn sàng nhập giá tiếp theo
      bidAmountField.clear();
      showBidSuccess(currentAuction.getItem().getName(), bidAmount);
    } catch (NumberFormatException e) {
        showError("Số tiền không hợp lệ. Vui lòng chỉ nhập số!");
      }
      catch (Exception e) {
      e.printStackTrace();
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

  /**
   * Phương thức gửi một yêu cầu thoát khỏi phiên đấu giá hiện tại.
   * Được gọi khi người dùng ấn vào bất kì buttion nào để chuyển sang trang khác.
   */
  public void leaveAuctionRoom() {
    stopCountdownTimer();
    ServerConnection.sendData(new LeaveRoomRequestDTO(SessionManager.getCurrentAuction().getId()));
  }
}
