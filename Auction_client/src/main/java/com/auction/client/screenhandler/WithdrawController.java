package com.auction.client.screenhandler;

import com.auction.client.network.SessionManager;
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