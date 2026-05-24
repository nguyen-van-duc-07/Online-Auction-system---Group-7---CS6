package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.shared.request.LoginRequestDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * Controller xử lý logic cho màn hình đăng nhập.
 */
public class LoginController {

  @FXML
  private TextField phoneNumberField;
  @FXML
  private PasswordField hiddenPasswordField;
  @FXML
  private TextField visiblePasswordField;
  @FXML
  private Label eyeIconShow;
  @FXML
  private Label eyeIconHide;

  private boolean isPasswordVisible = false;

  @FXML
  private void togglePasswordVisible(MouseEvent event) {
    // Chuyển sang trạng thái ngược lại
    isPasswordVisible = !isPasswordVisible;
    if (isPasswordVisible) {
      // Lấy String từ hiddenPasswordField rồi set cho visiblePasswordField
      visiblePasswordField.setText(hiddenPasswordField.getText());
      visiblePasswordField.setVisible(true);
      hiddenPasswordField.setVisible(false);
      eyeIconHide.setVisible(true);
      eyeIconShow.setVisible(false);
    } else {
      hiddenPasswordField.setText(visiblePasswordField.getText());
      hiddenPasswordField.setVisible(true);
      visiblePasswordField.setVisible(false);
      eyeIconHide.setVisible(false);
      eyeIconShow.setVisible(true);
    }
  }

  // Logic đăng nhập
  @FXML
  private void handleLogin() {
    String phoneNumber = phoneNumberField.getText();
    String password = isPasswordVisible ? visiblePasswordField.getText() : hiddenPasswordField.getText();

    // Kiểm tra xem người dùng đã nhập đủ thông tin chưa
    if (phoneNumber.trim().isEmpty() || password.trim().isEmpty()) {
      ScreenController.showAlert(Alert.AlertType.INFORMATION, null,
          "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!");
      return;
    }

    // Tạo tài khoản mới, mặc định là Bidder
    LoginRequestDTO loginReq = new LoginRequestDTO(phoneNumber, password);
    ServerConnection.sendData(loginReq);
  }

  @FXML
  private void goToRegister() {
    ScreenController.switchScreen("User/SignUp.fxml", "Đăng ký");
  }
}