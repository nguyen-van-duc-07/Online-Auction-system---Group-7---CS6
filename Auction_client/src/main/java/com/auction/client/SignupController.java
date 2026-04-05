package com.auction.client;

import com.auction.shared.model.Bidder;
import com.auction.shared.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class SignupController {

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
    private void togglePasswordVisible(MouseEvent event) {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            pwdVisible.setText(pwdHidden.getText());
            pwdVisible.setVisible(true);
            pwdHidden.setVisible(false);
            eyeHide.setVisible(true);
            eyeShow.setVisible(false);
        }
        else {
            pwdHidden.setText(pwdVisible.getText());
            pwdHidden.setVisible(true);
            pwdVisible.setVisible(false);
            eyeHide.setVisible(false);
            eyeShow.setVisible(true);
        }
    }
    @FXML
    private void toggleConfirmPasswordVisible(MouseEvent event) {
        isConfirmVisible = !isConfirmVisible;
        if (isConfirmVisible) {
            confirmPwdVisible.setText(confirmPwdHidden.getText());
            confirmPwdVisible.setVisible(true);
            confirmPwdHidden.setVisible(false);
            confirmEyeShow.setVisible(false);
            confirmEyeHide.setVisible(true);
        } else {
            confirmPwdHidden.setText(confirmPwdVisible.getText());
            confirmPwdHidden.setVisible(true);
            confirmPwdVisible.setVisible(false);
            confirmEyeShow.setVisible(true);
            confirmEyeHide.setVisible(false);
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = txtUser.getText();
        String password = isPasswordVisible ? pwdVisible.getText() : pwdHidden.getText();
        String confirm = isConfirmVisible ? confirmPwdVisible.getText() : confirmPwdHidden.getText();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            show("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!password.equals(confirm)) {
            show("Mật khẩu xác nhận không khớp!");
            return;
        }
        if (Main.userDatabase.containsKey(username)) {
            show("Tài khoản đã tồn tại!");
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
        show("Đăng ký thành công tài khoản: " + username);

        gotoLogin(event);
    }

    private void gotoLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Đăng nhập");
            stage.show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void show(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}