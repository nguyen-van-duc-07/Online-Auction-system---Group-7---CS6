package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.request.JoinRoomRequestDTO;
import com.auction.shared.request.LeaveRoomRequestDTO;
import com.auction.shared.request.PlaceBidRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.response.NewBidDTO;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    instance = this; // Gán màn hình hiện tại vào biến static
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
  public void leaveCurrentAuction() {
    if (countdownTimer != null) {
      countdownTimer.stop();
    }
    ServerConnection.sendData(new LeaveRoomRequestDTO(SessionManager.getCurrentAuction().getId()));
  }

  private void loadChartData() {
    priceSeries = new XYChart.Series<>();
    priceSeries.setName("Lịch sử giá");

    // 2. Xóa biểu đồ trắng cũ (nếu có) để chuẩn bị gắn dữ liệu mới
    priceChart.getData().clear();

    // 3. Lấy danh sách lịch sử từ Session (đã được Server trả về khi JoinRoom)
    List<BidTransaction> history = currentAuction.getBidHistory();

    // 4. Xử lý Logic Hiển thị
    if (history != null && !history.isEmpty()) {
      // Kịch bản A: Đã có người đấu giá -> Vẽ lại toàn bộ lịch sử
      for (BidTransaction tx : currentAuction.getBidHistory()) {
        // Controller tự format thời gian tại đây
        String formattedTime = tx.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        priceSeries.getData().add(new XYChart.Data<>(
                formattedTime,
                tx.getBidAmount().doubleValue()
        ));
      }
      }
     else {
      // Kịch bản B: Sàn đấu giá mới tinh -> Tạo điểm xuất phát
      String startTime = "Bắt đầu";

      // Lấy giá khởi điểm (hoặc giá cao nhất hiện tại do chưa ai bid)
      BigDecimal startPrice = currentAuction.getCurrentHighestPrice();

      priceSeries.getData().add(new XYChart.Data<>(startTime, startPrice.doubleValue()));

      // Bắn một thông báo nhỏ góc màn hình khích lệ người chơi
      Notifications.create()
              .title("Sàn đấu giá đã mở!")
              .text("Sản phẩm chưa có ai trả giá. Hãy là người dẫn đầu!")
              .hideAfter(javafx.util.Duration.seconds(8))
              .position(Pos.BOTTOM_RIGHT)
              .showInformation();
    }

    // 5. Gắn đường dữ liệu hoàn chỉnh vào biểu đồ
    priceChart.getData().add(priceSeries);
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
    leaveCurrentAuction();
    homeController.gotoResult();
  }

  @FXML
  public void gotoProfile() {
    leaveCurrentAuction();
    homeController.gotoProfile();
  }

  @FXML
  public void gotoLogin() {
    leaveCurrentAuction();
    homeController.gotoLogin();
  }

  @FXML
  public void gotoWallet() {
    leaveCurrentAuction();
    homeController.gotoWallet();
  }

  @FXML
  public void gotoHomeWithHyperLink() {
    leaveCurrentAuction();
    ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
  }

  @FXML
  public void gotoSellerHome() {
    leaveCurrentAuction();
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
                      SessionManager.getCurrentUser().getId(), SessionManager.getCurrentUser().getRealName(),
                      bidAmount);
      ServerConnection.sendData(req);
      // Xóa ô nhập để người dùng sẵn sàng nhập giá tiếp theo
      bidAmountField.clear();
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

  public void onNewBidReceived(NewBidDTO newBid) {
    Platform.runLater(() -> {
      // 1. Cập nhật giá mới nhất vào biến hiện tại
      currentAuction.setCurrentHighestPrice(newBid.getBidAmount());

      // 2. Chạy hiệu ứng nháy màu và cập nhật Text
      refreshPriceUI();

      // 3. Thêm điểm ảnh mới vào biểu đồ LineChart/AreaChart
      if (priceSeries != null) {
        // Lấy thời gian thực lúc nhận được Bid
        String currentTime = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Thêm điểm mới vào đường series hiện tại
        // Lưu ý: Dùng doubleValue() vì Chart yêu cầu kiểu Number
        priceSeries.getData().add(new XYChart.Data<>(currentTime, newBid.getBidAmount().doubleValue()));

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
        showBidSuccess(currentAuction.getItem().getName(), newBid.getBidAmount());
      }
    });
  }
}
