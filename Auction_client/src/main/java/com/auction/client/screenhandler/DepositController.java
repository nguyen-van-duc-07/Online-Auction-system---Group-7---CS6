package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.enums.WalletTransactionType;
import com.auction.shared.request.CreateTransactionRequestDTO;
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
 * Bộ điều khiển (Controller) cho màn hình nạp tiền vào ví của người dùng.
 * Quản lý ràng buộc nhập liệu và gửi giao dịch nạp tiền (deposit) lên Server.
 */
public class DepositController {
  private static final Logger log = LoggerFactory.getLogger(DepositController.class);

  @FXML private TextField amountTextField;

  // Khai báo một callback để truyền dữ liệu về màn hình ví
  private Consumer<BigDecimal> onSuccessCallback;

  /**
   * Thiết lập hàm callback để xử lý sự kiện sau khi nạp tiền thành công.
   *
   * @param onSuccessCallback hàm callback nhận số tiền nạp làm tham số
   */
  public void setOnSuccessCallback(Consumer<BigDecimal> onSuccessCallback) {
    this.onSuccessCallback = onSuccessCallback;
  }

  /**
   * Khởi tạo bộ điều khiển nạp tiền.
   * Thiết lập ràng buộc nhập số cho trường số tiền nạp.
   */
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

      // 2. THỰC HIỆN GỌI SERVICE QUA SOCKET
      // Lấy ID người dùng đang đăng nhập từ Session
      String currentUserId = SessionManager.getCurrentUser().getId();

      CreateTransactionRequestDTO requestDTO = CreateTransactionRequestDTO.builder()
          .userId(currentUserId)
          .amount(amount)
          .type(WalletTransactionType.DEPOSIT)
          .build();
      
      ServerConnection.sendData(requestDTO);

      // Đóng cửa sổ nạp tiền sau khi gửi request
      closeWindow(event);

    } catch (NumberFormatException e) {
      showAlert(Alert.AlertType.ERROR, "Lỗi", "Định dạng số tiền không hợp lệ!");
    } catch (Exception e) {
      log.error("Có lỗi xảy ra trong quá trình nạp tiền", e);
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