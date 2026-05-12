package com.auction.client.screenhandler;

import com.auction.client.network.SessionManager;
import service.WalletService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class WithdrawController {

  @FXML private TextField amountTextField;

  private WalletService walletService = new WalletService();
  private Consumer<BigDecimal> onSuccessCallback;

  public void setOnSuccessCallback(Consumer<BigDecimal> callback) {
    this.onSuccessCallback = callback;
  }

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

      // Kiểm tra số dư thực tế từ Database trước khi thực hiện lệnh
      BigDecimal currentBalance = walletService.getBalance(currentUserId);
      if (amount.compareTo(currentBalance) > 0) {
        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số dư không đủ!");
        return;
      }

      // Thực hiện rút tiền
      boolean isSuccess = walletService.withdraw(currentUserId, amount);

      if (isSuccess) {
        if (onSuccessCallback != null) {
          onSuccessCallback.accept(amount);
        }
        String formatted = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã rút thành công: " + formatted);
        closeWindow(event);
      } else {
        showAlert(Alert.AlertType.ERROR, "Lỗi", "Giao dịch thất bại. Vui lòng kiểm tra lại!");
      }

    } catch (NumberFormatException e) {
      showAlert(Alert.AlertType.ERROR, "Lỗi", "Định dạng không hợp lệ!");
    }
    catch (Exception e){
      e.printStackTrace();
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