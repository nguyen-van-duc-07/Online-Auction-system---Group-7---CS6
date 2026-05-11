package com.auction.client.screenhandler;

import com.auction.client.network.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import service.WalletService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Class có nhiệm vụ quản lý màn hình ví người dùng.
 */
public class WalletController {
  @FXML private Button depositButton;
  @FXML private Button withdrawButton;
  @FXML private Label balanceLabel;

  private WalletService walletService = new WalletService();

  @FXML
  public void initialize() {
    if (balanceLabel != null) {
      balanceLabel.setText("Đang tải...");
    }

    try {
      String currentUserId = SessionManager.getCurrentUser().getId();

      // Gọi hàm getBalance từ Service
      BigDecimal balance = walletService.getBalance(currentUserId);

      NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
      String formattedBalance = currencyFormat.format(balance);

      balanceLabel.setText(formattedBalance);

    } catch (Exception e) {
      e.printStackTrace();
      if (balanceLabel != null) {
        balanceLabel.setText("Lỗi kết nối");
      }
    }
  }

  @FXML
  public void gotoWithdraw(ActionEvent event) {
    ScreenController.createSubWindow(event, "User/Wallet/Withdraw.fxml", "Rút tiền");
  }

  @FXML
  public void gotoDeposit(ActionEvent event) {
    ScreenController.createSubWindow(event, "User/Wallet/Deposit.fxml", "Nạp tiền");
  }

  @FXML
  public void gotoLogin() {
    // Thay đổi đường dẫn FXML cho khớp với cấu trúc thư mục thực tế của bạn
    ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");
  }

  @FXML
  public void gotoProfile() {
    ScreenController.switchScreen("User/Profile.fxml", "Hồ sơ cá nhân");
  }

  @FXML
  public void gotoResult() {
    ScreenController.switchScreen("Bidder/Result.fxml", "Kết quả đấu giá");
  }

  @FXML
  public void gotoHomeWithHyperLink() {
    ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
  }
}