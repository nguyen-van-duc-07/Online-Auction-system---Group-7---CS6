package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.shared.request.SignUpRequestDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller xử lý logic cho màn hình đăng ký tài khoản.
 */
public class SignUpController {
  @FXML
  private TextField phoneNumberField;
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

  /**
   * Khởi tạo bộ điều khiển đăng ký tài khoản.
   * Ràng buộc sự kiện phím Enter cho tất cả các trường nhập liệu để kích hoạt đăng ký nhanh.
   */
  @FXML
  public void initialize() {
    phoneNumberField.setOnAction(event -> handleRegister());
    pwdHidden.setOnAction(event -> handleRegister());
    pwdVisible.setOnAction(event -> handleRegister());
    confirmPwdHidden.setOnAction(event -> handleRegister());
    confirmPwdVisible.setOnAction(event -> handleRegister());
  }

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
  private void handleRegister() {
    String phoneNumber = phoneNumberField.getText();
    String password = isPasswordVisible ? pwdVisible.getText() : pwdHidden.getText();
    String confirm = isConfirmVisible ? confirmPwdVisible.getText() : confirmPwdHidden.getText();

    // Ô nhập thông tin rỗng
    if (phoneNumber.trim().isEmpty() || password.trim().isEmpty() || confirm.trim().isEmpty()) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo",
          "Vui lòng nhập đầy đủ thông tin!");
      return;
    }

    // Nếu sai mật khẩu xác nhận
    if (!password.equals(confirm)) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo",
          "Mật khẩu xác nhận không khớp!");
      return;
    }

    // Nếu tất cả đều ổn thì sẽ gửi cho server
    SignUpRequestDTO signupReq = new SignUpRequestDTO(phoneNumber, password);
    ServerConnection.sendData(signupReq);

    phoneNumberField.clear();
    pwdHidden.clear();
    pwdVisible.clear();
    confirmPwdHidden.clear();
    confirmPwdVisible.clear();
  }

  /**
   * Chuyển hướng người dùng quay lại màn hình đăng nhập.
   */
  @FXML
  public void gotoLogin() {
    ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");
  }
}
