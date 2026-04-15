package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class SellerHomeController
{
    @FXML
    public void gotoHome(ActionEvent event)
    {
        ScreenController.switchScreen(event, "Home.fxml", "Trang chủ");
    }

    @FXML
    public void gotoLogin(ActionEvent event)
    {
        ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Xác nhận đăng xuất",
                "Bạn có chắc chắn muốn đăng xuất không?", event).ifPresent(response -> {
            if (response == ButtonType.OK)
            {
                ScreenController.switchScreen(event, "Login.fxml", "Đăng nhập");
            }
        });
    }

    @FXML
    public void gotoProfile(ActionEvent event)
    {
        ScreenController.switchScreen(event, "Profile.fxml", "Thông tin tài khoản");
    }

    @FXML
    public void gotoWallet(ActionEvent event)
    {
        ScreenController.switchScreen(event, "Wallet.fxml", "Ví người dùng");
    }
}