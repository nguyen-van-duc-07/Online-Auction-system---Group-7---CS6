package com.auction.client.screenhandler;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class DepositController {

  @FXML
  private TextField amountTextField;

  @FXML
  private Button confirmButton;

  @FXML
  private Button cancelButton;

  @FXML
  public void initialize() {
    // Ràng buộc nhập liệu: Chỉ cho phép số và tối đa một dấu chấm thập phân
    amountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.matches("\\d*(\\.\\d*)?")) {
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
      // Sử dụng String constructor để khởi tạo BigDecimal (chính xác nhất)
      BigDecimal amount = new BigDecimal(amountStr);

      // Kiểm tra số tiền phải lớn hơn 0
      if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số tiền nạp phải lớn hơn 0!");
        return;
      }

      // Giả lập định dạng tiền tệ VNĐ để hiển thị thông báo cho đẹp
      NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
      String formattedAmount = currencyFormat.format(amount);

      // =========================================================================
      // TODO: GỌI SERVICE XỬ LÝ GIAO DỊCH
      // service.deposit(userId, amount);
      // =========================================================================

      System.out.println("Đang xử lý nạp: " + amount + " thông qua BigDecimal");

      showAlert(Alert.AlertType.INFORMATION, "Thành công",
              "Đã nạp thành công: " + formattedAmount);

      closeWindow(event);

    } catch (NumberFormatException e) {
      showAlert(Alert.AlertType.ERROR, "Lỗi", "Định dạng số tiền không hợp lệ!");
    }
  }

  @FXML
  void handleCancel(ActionEvent event) {
    closeWindow(event);
  }

  private void closeWindow(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.close();
  }

  private void showAlert(Alert.AlertType alertType, String title, String message) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}