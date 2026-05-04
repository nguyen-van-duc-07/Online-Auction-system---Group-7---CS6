package com.auction.client.screenhandler;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Controller xử lý logic cho màn hình kết quả đấu giá.
 */
public class ResultController {
  HomeController homeController = new HomeController();

  @FXML
  public void gotoLogin() {
    homeController.gotoLogin();
  }

  @FXML
  public void gotoSellerHome() {
    homeController.gotoSellerHome();
  }

  @FXML
  public void gotoProfile() {
    homeController.gotoProfile();
  }

  @FXML
  public void gotoWallet() {
    homeController.gotoWallet();
  }
}
