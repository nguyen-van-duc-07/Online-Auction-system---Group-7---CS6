package com.auction.client;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Controller xử lý logic cho màn hình trang chủ.
 */
public class HomeController {
  @FXML
  public void gotoLogin(ActionEvent event) {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Xác nhận đăng xuất",
        "Bạn có chắc chắn muốn đăng xuất không?", event).ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        ScreenController.switchScreen(event, "Login.fxml", "Đăng nhập");
      }
    });
  }

  @FXML
  public void gotoSellerHome(ActionEvent event) {
    ScreenController.switchScreen(event, "SellerHome.fxml", "Quản lý sản phẩm (Seller)");
  }

  @FXML
  public void gotoProfile(ActionEvent event) {
    ScreenController.switchScreen(event, "Profile.fxml", "Thông tin tài khoản");
  }

  @FXML
  public void gotoWallet(ActionEvent event) {
    ScreenController.switchScreen(event, "Wallet.fxml", "Ví người dùng");
  }

  @FXML
  public void gotoResult(ActionEvent event) {
    ScreenController.switchScreen(event, "Result.fxml", "Kết quả đấu giá");
  }
}