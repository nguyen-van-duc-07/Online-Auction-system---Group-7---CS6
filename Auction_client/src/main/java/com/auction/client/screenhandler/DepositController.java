package com.auction.client.screenhandler;

import com.auction.client.network.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.WalletService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class DepositController {

  @FXML private TextField amountTextField;

  // Khai báo một callback để truyền dữ liệu về màn hình ví
  private Consumer<BigDecimal> onSuccessCallback;
  private WalletService walletService = new WalletService();

  // Setter để màn hình chính truyền hàm xử lý vào
  public void setOnSuccessCallback(Consumer<BigDecimal> onSuccessCallback) {
    this.onSuccessCallback = onSuccessCallback;
  }

  @FXML
  public void initialize() {
    // Ràng buộc nhập liệu: Chỉ cho phép số và tối đa một dấu chấm thập phân
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
      // Sử dụng String constructor để khởi tạo BigDecimal (chính xác nhất)
      BigDecimal amount = new BigDecimal(amountStr);

      // Kiểm tra số tiền phải lớn hơn 0
      if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số tiền nạp phải lớn hơn 0!");
        return;
      }
      // Kiểm tra số tiền tối thiểu (Ví dụ: 1000VNĐ)
      BigDecimal minDeposit = new BigDecimal("1000");
      if (amount.compareTo(minDeposit) < 0) {
        showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số tiền nạp tối thiểu là 1.000đ");
        return;
      }

      // 2. THỰC HIỆN GỌI SERVICE ĐỂ LƯU VÀO DATABASE
      // Lấy ID người dùng đang đăng nhập từ Session
      String currentUserId = SessionManager.getCurrentUser().getId();

      // Gọi hàm deposit (hàm này sử dụng SQL UPDATE wallets SET balance = balance + ? ...)
      boolean isSuccess = walletService.deposit(currentUserId, amount);

      if (isSuccess) {
        // CHỈ KHI DATABASE CẬP NHẬT THÀNH CÔNG MỚI CHẠY TIẾP
        if (onSuccessCallback != null) {
          onSuccessCallback.accept(amount);
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedAmount = currencyFormat.format(amount);

        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã nạp thành công: " + formattedAmount);
        closeWindow(event);
      } else {
        // Nếu database trả về false (lỗi kết nối hoặc không tìm thấy user_id trong bảng wallets)
        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thực hiện giao dịch vào Database!");
      }

    } catch (NumberFormatException e) {
      showAlert(Alert.AlertType.ERROR, "Lỗi", "Định dạng số tiền không hợp lệ!");
    } catch (Exception e) {
      e.printStackTrace();
      showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Có lỗi xảy ra: " + e.getMessage());
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