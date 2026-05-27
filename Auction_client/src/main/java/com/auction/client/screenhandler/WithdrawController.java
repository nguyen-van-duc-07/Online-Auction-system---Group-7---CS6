package com.auction.client.screenhandler;

import com.auction.client.network.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Bộ điều khiển (Controller) cho màn hình rút tiền từ ví của người dùng.
 * Quản lý kiểm tra và gửi giao dịch rút tiền (withdraw) lên Server.
 */
public class WithdrawController {
  private static final Logger log = LoggerFactory.getLogger(WithdrawController.class);

  @FXML private TextField amountTextField;
  private Consumer<BigDecimal> onSuccessCallback;

  /**
   * Thiết lập hàm callback xử lý sự kiện sau khi rút tiền thành công.
   *
   * @param callback hàm callback nhận số tiền rút làm tham số
   */
  public void setOnSuccessCallback(Consumer<BigDecimal> callback) {
    this.onSuccessCallback = callback;
  }

  /**
   * Khởi tạo bộ điều khiển rút tiền.
   * Thiết lập ràng buộc nhập số cho ô số tiền rút.
   */
  @FXML
  public void initialize() {
    amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.matches("\\d*")) {
        amountTextField.setText(oldValue);
      }
    });
  }

  @FXML
  void handleConfirm(ActionEvent event) {
    String amountStr = amountTextField.getText().trim();
    if (amountStr.isEmpty()) {
      showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số tiền!");
      return;
    }

    try {
      BigDecimal amount = new BigDecimal(amountStr);
      if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số tiền rút phải lớn hơn 0!");
        return;
      }
      // Kiểm tra số tiền tối thiểu (Ví dụ: 5000VNĐ)
      BigDecimal minWithdraw = new BigDecimal("5000");
      if (amount.compareTo(minWithdraw) < 0) {
        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số tiền rút tối thiểu là 5.000đ");
        return;
      }

      String currentUserId = SessionManager.getCurrentUser().getId();

      com.auction.shared.request.CreateTransactionRequestDTO requestDTO = com.auction.shared.request.CreateTransactionRequestDTO.builder()
          .userId(currentUserId)
          .amount(amount)
          .type(com.auction.shared.enums.WalletTransactionType.WITHDRAW)
          .build();
      
      com.auction.client.network.ServerConnection.sendData(requestDTO);

      closeWindow(event);

    } catch (NumberFormatException e) {
      showAlert(Alert.AlertType.ERROR, "Lỗi", "Định dạng không hợp lệ!");
    }
    catch (Exception e){
      log.error("Có lỗi xảy ra khi thực hiện rút tiền", e);
    }
  }

  @FXML void handleCancel(ActionEvent event) { closeWindow(event); }

  private void closeWindow(ActionEvent event) {
    ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
  }

  private void showAlert(Alert.AlertType type, String title, String msg) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(msg);
    alert.showAndWait();
  }
}