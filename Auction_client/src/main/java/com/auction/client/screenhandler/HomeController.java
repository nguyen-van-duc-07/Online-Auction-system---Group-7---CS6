package com.auction.client.screenhandler;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Controller xử lý logic cho màn hình trang chủ.
 */
public class HomeController {
  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Xác nhận đăng xuất",
        "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        ScreenController.switchScreen("Login.fxml", "Đăng nhập");
      }
    });
  }

  @FXML
  public void gotoSellerHome() {
    ScreenController.switchScreen("SellerHome.fxml", "Quản lý sản phẩm (Seller)");
  }

  @FXML
  public void gotoProfile() {
    ScreenController.switchScreen("Profile.fxml", "Thông tin tài khoản");
  }

  @FXML
  public void gotoWallet() {
    ScreenController.switchScreen("Wallet.fxml", "Ví người dùng");
  }

  @FXML
  public void gotoResult() {
    ScreenController.switchScreen("Result.fxml", "Kết quả đấu giá");
  }
}