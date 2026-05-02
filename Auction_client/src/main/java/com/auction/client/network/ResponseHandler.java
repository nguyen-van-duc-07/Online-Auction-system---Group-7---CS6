package com.auction.client.network;

import com.auction.client.screenhandler.ScreenController;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ResponseHandler {
  // Xử lý phản hồi về yêu cầu login của server
  public static void login(String msg) {
    // Nếu yêu cầu xử lý thành công
    if ("LOGIN_SUCCESS".equals(msg)) {
      Platform.runLater(() -> {
        ScreenController.switchScreen("Home.fxml", "Trang chủ");
      });

      // Nếu yêu cầu xử lý không thành công
    } else if ("LOGIN_FAILED".equals(msg)) {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi đăng nhập",
            "Tài khoản hoặc mật khẩu không chính xác");
      });
    }
  }

  // Xử lý phản hồi về yêu cầu signup của server
  public static void signup(String msg) {
    // Nếu yêu cầu xử lý thành công
    if ("SIGNUP_SUCCESS".equals(msg)) {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.INFORMATION, null,
        "Đăng ký tài khoản thành công!").ifPresent(Response -> {
          if (Response == ButtonType.OK) {
            ScreenController.switchScreen("Login.fxml", "Đăng nhập");
          }
        });
      });

      // Nếu yêu cầu xử lý không thành công
    } else if ("SIGNUP_FAILED".equals(msg)) {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.WARNING, null,
          "Tài khoản đã tồn tại!");
      });
    }
  }
}
