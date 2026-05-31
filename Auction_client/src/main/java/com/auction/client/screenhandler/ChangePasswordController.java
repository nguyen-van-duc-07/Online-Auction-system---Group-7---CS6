package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.request.ChangePasswordRequestDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bộ điều khiển (Controller) cho giao diện đổi mật khẩu người dùng (ChangePasswordForm).
 * Cho phép người dùng nhập mật khẩu hiện tại và thiết lập mật khẩu mới.
 */
public class ChangePasswordController {
  private static final Logger log = LoggerFactory.getLogger(ChangePasswordController.class);

  private static ChangePasswordController instance;

  /**
   * Lấy instance duy nhất đang hoạt động của ChangePasswordController.
   *
   * @return đối tượng ChangePasswordController hiện tại
   */
  public static ChangePasswordController getInstance() {
    return instance;
  }

  @FXML private PasswordField currentPasswordField;
  @FXML private PasswordField newPasswordField;
  @FXML private PasswordField confirmPasswordField;

  /**
   * Khởi tạo bộ điều khiển đổi mật khẩu.
   * Gán instance hiện tại vào biến static.
   */
  @FXML
  public void initialize() {
    instance = this;
  }

  /**
   * Xử lý sự kiện khi người dùng nhấn xác nhận cập nhật mật khẩu mới.
   * Thực hiện xác thực các trường nhập liệu trước khi gửi yêu cầu đổi mật khẩu lên Server.
   */
  @FXML
  public void handleUpdatePassword() {
    String currentPassword = currentPasswordField.getText().trim();
    String newPassword = newPasswordField.getText().trim();
    String confirmPassword = confirmPasswordField.getText().trim();

    if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
      return;
    }

    if (!newPassword.equals(confirmPassword)) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Mật khẩu mới và mật khẩu xác nhận không khớp!");
      return;
    }

    if (SessionManager.getCurrentUser() == null) {
      ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin phiên làm việc hiện tại!");
      return;
    }

    String userId = SessionManager.getCurrentUser().getId();
    ChangePasswordRequestDTO request = new ChangePasswordRequestDTO(userId, currentPassword, newPassword);
    
    log.info("Gửi yêu cầu đổi mật khẩu lên Server...");
    ServerConnection.sendData(request);
  }

  /**
   * Đóng cửa sổ phụ đang hiển thị form đổi mật khẩu.
   */
  public void closeWindow() {
    if (currentPasswordField != null && currentPasswordField.getScene() != null) {
      Stage stage = (Stage) currentPasswordField.getScene().getWindow();
      stage.close();
    }
  }
}
