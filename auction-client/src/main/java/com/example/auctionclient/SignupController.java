package com.example.auctionclient;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class SignupController {
    @FXML private TextField usernamefield, realnamefield, passwordfield, dobfield;
    @FXML private Label error;

    @FXML
    public void handleSignup(ActionEvent event) {
        // Gói dữ liệu
        String json = String.format(
                "{\"accountname\":\"%s\", \"realname\":\"%s\", \"password\":\"%s\", \"dob\":\"%s\"}",
                usernamefield.getText(), realnamefield.getText(), passwordfield.getText(), dobfield.getText()
        );

        // Gửi sang Server
        String result = ApiService.post("/signup", json);

        if ("OK".equals(result)) {
            System.out.println("Đăng ký xong, quay lại Login thôi!");
        } else if ("EXISTED".equals(result)) {
            error.setText("Tài khoản đã tồn tại!");
        } else {
            error.setText("Lỗi đăng ký!");
        }
    }
}