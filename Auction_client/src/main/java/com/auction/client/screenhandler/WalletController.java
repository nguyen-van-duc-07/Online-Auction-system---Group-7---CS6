package com.auction.client.screenhandler;

import com.auction.client.network.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.WalletService;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import static com.auction.client.screenhandler.ScreenController.primaryStage;
import static com.auction.client.screenhandler.ScreenController.showAlert;

/**
 * Class có nhiệm vụ quản lý màn hình ví người dùng.
 */
public class WalletController {
  HomeController homeController = new HomeController();
  @FXML private Label balanceLabel;

  private WalletService walletService = new WalletService();

  @FXML
  public void initialize() {
    // Gọi hàm load dữ liệu khi mở màn hình lần đầu
    refreshBalance();
  }

  /**
   * Tách riêng logic lấy và hiển thị số dư thành một hàm độc lập.
   * Gọi hàm này mỗi khi cần cập nhật lại giao diện ngay lập tức.
   */
  private void refreshBalance() {
    if (balanceLabel != null) {
      balanceLabel.setText("Đang tải...");
    }

    try {
      String currentUserId = SessionManager.getCurrentUser().getId();

      // Gọi hàm getBalance từ Service lấy số dư mới nhất từ DB
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
  public void gotoLogin() {
    homeController.gotoLogin();
  }

  @FXML
  public void gotoProfile() {
    homeController.gotoProfile();
  }

  @FXML
  public void gotoWithdraw(ActionEvent event) {
    WithdrawController withdrawController = ScreenController.createSubWindowAndGetController("User/Wallet/Withdraw.fxml", "Rút tiền");
    if (withdrawController != null) {
      withdrawController.setOnSuccessCallback(amount -> {
        // Tự động cập nhật lại balanceLabel ngay lập tức
        refreshBalance();
      });
    }
  }

  @FXML
  public void gotoDeposit(ActionEvent event) {
    DepositController depositController = ScreenController.createSubWindowAndGetController("User/Wallet/Deposit.fxml", "Nạp tiền");

    // 2. Truyền Callback vào Controller vừa lấy được
    if (depositController != null) {
      depositController.setOnSuccessCallback(amount -> {
        refreshBalance();
      });
    }
  }

  @FXML
  public void gotoResult() {
    homeController.gotoResult();
  }

  @FXML
  public void gotoHomeWithHyperLink() {
    ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
  }
}