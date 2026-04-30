package com.auction.client;

import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller xử lý logic cho màn hình đăng ký tài khoản.
 */
public class SignupController {
  @FXML
  private TextField txtUser;
  @FXML
  private PasswordField pwdHidden;
  @FXML
  private TextField pwdVisible;
  @FXML
  private PasswordField confirmPwdHidden;
  @FXML
  private TextField confirmPwdVisible;
  @FXML
  private Label eyeShow;
  @FXML
  private Label eyeHide;
  @FXML
  private Label confirmEyeShow;
  @FXML
  private Label confirmEyeHide;

  private boolean isPasswordVisible = false;
  private boolean isConfirmVisible = false;

  // Tạo một hàm dùng chung để xử lý logic ẩn/hiện
  private void updateVisibility(boolean isVisible, TextField visibleField,
                                PasswordField hiddenField, Label showLabel, Label hideLabel) {
    if (isVisible) {
      visibleField.setText(hiddenField.getText());
      visibleField.setVisible(true);
      hiddenField.setVisible(false);
      hideLabel.setVisible(true);
      showLabel.setVisible(false);
    } else {
      hiddenField.setText(visibleField.getText());
      hiddenField.setVisible(true);
      visibleField.setVisible(false);
      hideLabel.setVisible(false);
      showLabel.setVisible(true);
    }
  }

  // Gọi hàm dùng chung cho ô Mật khẩu
  @FXML
  private void togglePasswordVisible() {
    isPasswordVisible = !isPasswordVisible;
    updateVisibility(isPasswordVisible, pwdVisible, pwdHidden, eyeShow, eyeHide);
  }

  // Gọi hàm dùng chung cho ô Xác nhận mật khẩu
  @FXML
  private void toggleConfirmPasswordVisible() {
    isConfirmVisible = !isConfirmVisible;
    updateVisibility(isConfirmVisible, confirmPwdVisible,
        confirmPwdHidden, confirmEyeShow, confirmEyeHide);
  }

  @FXML
  private void handleRegister(ActionEvent event) {
    String username = txtUser.getText();
    String password = isPasswordVisible ? pwdVisible.getText() : pwdHidden.getText();
    String confirm = isConfirmVisible ? confirmPwdVisible.getText() : confirmPwdHidden.getText();

    // Ô nhập thông tin rỗng
    if (username.trim().isEmpty() || password.trim().isEmpty() || confirm.trim().isEmpty()) {
      ScreenController.showAlert(Alert.AlertType.WARNING, null,
          "Vui lòng nhập đầy đủ thông tin!", event);
      return;
    }

    // Nếu sai mật khẩu xác nhận
    if (!password.equals(confirm)) {
      ScreenController.showAlert(Alert.AlertType.WARNING, null,
          "Mật khẩu xác nhận không khớp!", event);
      return;
    }

    // Nếu tài khoản đã tồn tại
    if (Main.userDatabase.containsKey(username)) {
      ScreenController.showAlert(Alert.AlertType.WARNING, null,
          "Tài khoản đã tồn tại!", event);
      txtUser.clear();
      pwdVisible.clear();
      pwdHidden.clear();
      confirmPwdVisible.clear();
      confirmPwdHidden.clear();
      return;
    }

    // Tạo tài khoản mới, mặc định là Bidder
    User user = new Bidder(username, password);

    Main.userDatabase.put(username, user);
    ScreenController.showAlert(Alert.AlertType.INFORMATION, null,
        "Đăng ký thành công tài khoản: " + username, event);

    gotoLogin(event);
  }

  @FXML
  private void gotoLogin(ActionEvent event) {
    ScreenController.switchScreen(event, "Login.fxml", "Đăng nhập");
  }
}