package com.auction.client.screenhandler;

import com.auction.shared.model.auction.Auction; // Đảm bảo đúng path này
import com.auction.shared.model.item.Item;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.time.Duration;
import java.time.LocalDateTime;

public class ItemAuctionController {

  private HomeController homeController = new HomeController();

  @FXML private Label productNameLabel;
  @FXML private Label currentPriceLabel;
  @FXML private Label timeRemainingLabel;
  @FXML private Label highestBidderLabel;
  @FXML private Label productDescLabel;
  @FXML private TextField bidAmountField;
  @FXML private Button placeBidButton;

  /**
   * Hàm này nhận đối tượng Auction thực tế từ nhóm bạn
   */
  public void setAuctionData(Auction auction) {
    if (auction != null && auction.getItem() != null) {
      Item item = auction.getItem();

      // 1. Hiển thị thông tin từ Item (Sử dụng Getter từ Lombok)
      productNameLabel.setText(item.getName());
      productDescLabel.setText(item.getDescription());

      // 2. Hiển thị giá hiện tại (Vì là BigDecimal nên format như sau)
      if (auction.getCurrentHighestPrice() != null) {
        String formattedPrice = String.format("%,.0f VNĐ", auction.getCurrentHighestPrice());
        currentPriceLabel.setText(formattedPrice);
      }

      // 3. Hiển thị người đấu giá cao nhất (Sử dụng ID vì model chưa có Name)
      if (auction.getHighestBidderId() != null && !auction.getHighestBidderId().isEmpty()) {
        highestBidderLabel.setText("ID: " + auction.getHighestBidderId());
      } else {
        highestBidderLabel.setText("Chưa có người đấu giá");
      }

      // 4. Tính toán thời gian còn lại (Vì Model chỉ có endTime)
      updateTimeRemaining(auction.getEndTime());

      // 5. Gợi ý mức giá đấu tiếp theo (Giá hiện tại + bước giá tối thiểu)
      if (auction.getMinStepPrice() != null) {
        java.math.BigDecimal nextBid = auction.getCurrentHighestPrice().add(auction.getMinStepPrice());
        bidAmountField.setPromptText("Tối thiểu: " + String.format("%,.0f", nextBid));
      }
    }
  }

  /**
   * Hàm phụ trợ tính toán thời gian còn lại từ LocalDateTime
   */
  private void updateTimeRemaining(LocalDateTime endTime) {
    if (endTime == null) {
      timeRemainingLabel.setText("Không xác định");
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    if (now.isAfter(endTime)) {
      timeRemainingLabel.setText("Đã kết thúc");
    } else {
      Duration duration = Duration.between(now, endTime);
      long hours = duration.toHours();
      long minutes = duration.toMinutesPart();
      long seconds = duration.toSecondsPart();

      // Format hiển thị kiểu HH:mm:ss
      timeRemainingLabel.setText(String.format("%02d : %02d : %02d", hours, minutes, seconds));
    }
  }

  @FXML
  public void handlePlaceBid() {
    // Lấy giá trị từ TextField và gọi hàm applyBid trong Model của bạn
    System.out.println("Gửi lệnh bid lên server...");
  }

  // --- CÁC HÀM ĐIỀU HƯỚNG ---
  @FXML public void gotoResult() { homeController.gotoResult(); }
  @FXML public void gotoProfile() { homeController.gotoProfile(); }
  @FXML public void gotoLogin() { homeController.gotoLogin(); }
  @FXML public void gotoWallet() { homeController.gotoWallet(); }
  @FXML public void gotoHomeWithHyperLink() { ScreenController.switchScreen("Home.fxml", "Trang chủ"); }
}