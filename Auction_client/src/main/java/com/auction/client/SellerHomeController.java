package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class SellerHomeController {
    HomeController homecontroller = new HomeController();
    @FXML
    public void gotoHome(ActionEvent event) {
        ScreenController.switchScreen(event, "Home.fxml", "Trang chủ");
    }

    @FXML
    public void gotoLogin(ActionEvent event) {
        homecontroller.gotoLogin(event);
    }

    @FXML
    public void gotoProfile(ActionEvent event) {
        homecontroller.gotoProfile(event);
    }

    @FXML
    public void gotoWallet(ActionEvent event) {
        homecontroller.gotoWallet(event);
    }

    @FXML
    public void gotoResult(ActionEvent event) {
        homecontroller.gotoResult(event);
    }
}