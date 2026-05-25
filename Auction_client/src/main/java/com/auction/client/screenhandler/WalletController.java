package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.shared.request.GetBalanceRequestDTO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Class có nhiệm vụ quản lý màn hình ví người dùng.
 */
public class WalletController {
  private static final Logger log = LoggerFactory.getLogger(WalletController.class);

  @FXML private Label balanceLabel;

  private static WalletController instance;

  public static WalletController getInstance() {
    return instance;
  }

  @FXML
  public void initialize() {
    instance = this;
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
    log.info("Gửi yêu cầu lấy số dư lên Server...");
    GetBalanceRequestDTO request = new GetBalanceRequestDTO();
    ServerConnection.sendData(request);
  }

  /**
   * Hàm này sẽ được gọi từ ResponseHandler sau khi có kết quả mạng trả về.
   */
  public void updateBalanceUI(BigDecimal currentBalance) {
    // Bắt buộc dùng Platform.runLater vì luồng mạng không được phép đổi giao diện trực tiếp
    Platform.runLater(() -> {
      if (balanceLabel != null) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedBalance = currencyFormat.format(currentBalance);
        balanceLabel.setText(formattedBalance);
      }
    });
  }

  public void showErrorUI() {
    Platform.runLater(() -> {
      if (balanceLabel != null) {
        balanceLabel.setText("Lỗi kết nối");
      }
    });
  }

  @FXML
  public void gotoLogin() {
    MainLayoutController.getInstance().gotoLogin();
  }

  @FXML
  public void gotoProfile() {
    MainLayoutController.getInstance().gotoProfile();
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
    MainLayoutController.getInstance().gotoResult();
  }

  @FXML
  public void gotoHomeWithHyperLink() {
    ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
  }
}