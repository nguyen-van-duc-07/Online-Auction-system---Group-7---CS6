package com.auction.client;

import javafx.event.ActionEvent;
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
  private TextField txtLoginUser;
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
  private void handleLogin(ActionEvent event) {
    String username = txtLoginUser.getText();
    String password = isPasswordVisible ? visiblePasswordField.getText() : hiddenPasswordField.getText();

    // 1. Kiểm tra xem người dùng đã nhập đủ thông tin chưa
    if (username.trim().isEmpty() || password.trim().isEmpty()) {
      ScreenController.showAlert(Alert.AlertType.INFORMATION, null, "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!", event);
      return;
    }

    // 2. Kiểm tra xem tài khoản có tồn tại trong bộ nhớ tạm chưa
    if (!Main.userDatabase.containsKey(username)) {
      // NẾU CHƯA CÓ TÀI KHOẢN
      ScreenController.showAlert(Alert.AlertType.INFORMATION, null, "Chưa có tài khoản!", event);
      return;
    }

    // 3. Nếu tài khoản có tồn tại, kiểm tra xem mật khẩu có khớp không
    if (!Main.userDatabase.get(username).getPassword().equals(password)) {
      // NẾU SAI MẬT KHẨU
      ScreenController.showAlert(Alert.AlertType.INFORMATION, null, "Mật khẩu không chính xác!", event);
      return;
    }

    // 4. Vượt qua hết các lỗi trên -> Đăng nhập thành công!
    ScreenController.switchScreen(event, "Home.fxml", "Trang chủ");
  }

  @FXML
  private void goToRegister(ActionEvent event) {
    ScreenController.switchScreen(event, "Signup.fxml", "Đăng ký");
  }
}