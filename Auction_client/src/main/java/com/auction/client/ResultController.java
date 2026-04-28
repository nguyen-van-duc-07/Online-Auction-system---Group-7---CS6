package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ResultController {
    HomeController homeController = new HomeController();

    @FXML
    public void gotoLogin(ActionEvent event) {
        homeController.gotoLogin(event);
    }

    @FXML
    public void gotoSellerHome(ActionEvent event) {
        homeController.gotoSellerHome(event);
    }

    @FXML
    public void gotoProfile(ActionEvent event) {
        homeController.gotoProfile(event);
    }

    @FXML
    public void gotoWallet(ActionEvent event) {
        homeController.gotoWallet(event);
    }
}
