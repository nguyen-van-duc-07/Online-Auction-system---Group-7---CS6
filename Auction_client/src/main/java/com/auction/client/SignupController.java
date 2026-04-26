package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class SignupController
{
    @FXML private TextField txtUser;
    @FXML private PasswordField pwdHidden;
    @FXML private TextField pwdVisible;
    @FXML private PasswordField confirmPwdHidden;
    @FXML private TextField confirmPwdVisible;
    @FXML private Label eyeShow;
    @FXML private Label eyeHide;
    @FXML private Label confirmEyeShow;
    @FXML private Label confirmEyeHide;

    private boolean isPasswordVisible = false;
    private boolean isConfirmVisible = false;

    @FXML
    private void togglePasswordVisible(MouseEvent event)
    {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible)
        {
            pwdVisible.setText(pwdHidden.getText());
            pwdVisible.setVisible(true);
            pwdHidden.setVisible(false);
            eyeHide.setVisible(true);
            eyeShow.setVisible(false);
        }
        else
        {
            pwdHidden.setText(pwdVisible.getText());
            pwdHidden.setVisible(true);
            pwdVisible.setVisible(false);
            eyeHide.setVisible(false);
            eyeShow.setVisible(true);
        }
    }
    @FXML
    private void toggleConfirmPasswordVisible(MouseEvent event)
    {
        isConfirmVisible = !isConfirmVisible;
        if (isConfirmVisible)
        {
            confirmPwdVisible.setText(confirmPwdHidden.getText());
            confirmPwdVisible.setVisible(true);
            confirmPwdHidden.setVisible(false);
            confirmEyeShow.setVisible(false);
            confirmEyeHide.setVisible(true);
        }
        else
        {
            confirmPwdHidden.setText(confirmPwdVisible.getText());
            confirmPwdHidden.setVisible(true);
            confirmPwdVisible.setVisible(false);
            confirmEyeShow.setVisible(true);
            confirmEyeHide.setVisible(false);
        }
    }

    @FXML
    private void handleRegister(ActionEvent event)
    {
        String username = txtUser.getText();
        String password = isPasswordVisible ? pwdVisible.getText() : pwdHidden.getText();
        String confirm = isConfirmVisible ? confirmPwdVisible.getText() : confirmPwdHidden.getText();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) // Ô nhập thông tin rỗng
        {
            ScreenController.showAlert(Alert.AlertType.INFORMATION, null, "Vui lòng nhập đầy đủ thông tin!", event);
            return;
        }

        if (!password.equals(confirm)) // Nếu sai mật khẩu xác nhận
        {
            ScreenController.showAlert(Alert.AlertType.INFORMATION, null, "Mật khẩu xác nhận không khớp!", event);
            return;
        }
        if (Main.userDatabase.containsKey(username)) // Nếu tài khoản đã tồn tại
        {
            ScreenController.showAlert(Alert.AlertType.INFORMATION, null, "Tài khoản đã tồn tại!", event);
            txtUser.setText("");
            if (isPasswordVisible) {pwdVisible.clear();}
            else {pwdHidden.clear();}
            if (isConfirmVisible) {confirmPwdVisible.clear();}
            else {confirmPwdHidden.clear();}
            return;
        }

        // Tạo tài khoản mới, mặc định là Bidder
        User user = new Bidder(username, password);

        Main.userDatabase.put(username, user);
        ScreenController.showAlert(Alert.AlertType.INFORMATION, null, "Đăng ký thành công tài khoản: " + username, event);

        gotoLogin(event);
    }

    @FXML
    private void gotoLogin(ActionEvent event)
    {
        ScreenController.switchScreen(event, "Login.fxml", "Đăng nhập");
    }
}