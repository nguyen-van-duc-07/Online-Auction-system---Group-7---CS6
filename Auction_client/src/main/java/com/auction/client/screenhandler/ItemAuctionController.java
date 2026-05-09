package com.auction.client.screenhandler;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ItemAuctionController {
  private HomeController homeController = new HomeController();

  @FXML
  private Label itemNameLabel;
  @FXML
  private Label currentPriceLabel;

  @FXML
  public void gotoResult() {
    homeController.gotoResult();
  }

  @FXML
  public void gotoProfile() {
    homeController.gotoProfile();
  }

  @FXML
  public void gotoLogin() {
    homeController.gotoLogin();
  }

  @FXML
  public void gotoWallet() {
    homeController.gotoWallet();
  }

  @FXML
  public void gotoHomeWithHyperLink() {
    ScreenController.switchScreen("Home.fxml", "Trang chủ");
  }
}
