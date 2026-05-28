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
 * Bộ điều khiển (Controller) cho giao diện quản lý ví điện tử cá nhân (Wallet).
 * Quản lý việc hiển thị số dư khả dụng, gọi dịch vụ nạp/rút tiền thông qua các cửa sổ phụ.
 */
public class WalletController {
  private static final Logger log = LoggerFactory.getLogger(WalletController.class);

  @FXML private Label balanceLabel;

  private static WalletController instance;

  /**
   * Lấy instance duy nhất đang hoạt động của WalletController.
   *
   * @return đối tượng WalletController hiện tại
   */
  public static WalletController getInstance() {
    return instance;
  }

  /**
   * Khởi tạo bộ điều khiển ví điện tử.
   * Gán instance hiện tại và thực hiện yêu cầu tải số dư từ Server.
   */
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
  /**
   * Cập nhật số dư hiển thị lên giao diện người dùng ví.
   *
   * @param currentBalance số dư ví khả dụng hiện tại
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

  /**
   * Hiển thị thông báo lỗi kết nối khi không thể lấy số dư ví từ Server.
   */
  public void showErrorUI() {
    Platform.runLater(() -> {
      if (balanceLabel != null) {
        balanceLabel.setText("Lỗi kết nối");
      }
    });
  }

  /**
   * Mở cửa sổ phụ thực hiện yêu cầu rút tiền từ ví.
   *
   * @param event sự kiện ActionEvent được kích hoạt từ nút bấm JavaFX
   */
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

  /**
   * Mở cửa sổ phụ thực hiện yêu cầu nạp tiền vào ví.
   *
   * @param event sự kiện ActionEvent được kích hoạt từ nút bấm JavaFX
   */
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
}