package com.example.auctionclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField usernameTextfield;
    @FXML private PasswordField passwordTextfield;
    @FXML private Label incorrect;

    @FXML
    public void loginButtonOnAction(ActionEvent event) {
        String user = usernameTextfield.getText();
        String pass = passwordTextfield.getText();

        if (user.isBlank() || pass.isBlank()) {
            incorrect.setText("Vui lòng nhập đủ thông tin!");
            return;
        }

        String json = String.format("{\"accountname\":\"%s\", \"password\":\"%s\"}", user, pass);
        String result = ApiService.post("/login", json);

        if ("EXACT".equals(result)) {
            incorrect.setText("Đăng nhập thành công!");
            // Chỗ này sau này ông viết thêm chuyển cảnh
        } else if ("WRONG".equals(result)) {
            incorrect.setText("Sai tài khoản hoặc mật khẩu!");
        } else {
            incorrect.setText("Lỗi kết nối Server!");
        }
    }
}