package com.auction.client.screenhandler;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Class có nhiệm vụ quản lý màn hình seller.
 */
public class SellerHomeController {
  HomeController homecontroller = new HomeController();

  @FXML
  public void gotoHomeWithHyperLink() {
    ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
  }

  @FXML
  public void gotoLogin() {
    homecontroller.gotoLogin();
  }

  @FXML
  public void gotoProfile() {
    homecontroller.gotoProfile();
  }

  @FXML
  public void gotoWallet() {
    homecontroller.gotoWallet();
  }

  @FXML
  public void gotoResult() {
    homecontroller.gotoResult();
  }

  @FXML
  public void gotoUploadItem() {
    ScreenController.switchScreen("Seller/UploadItem.fxml", "Đăng sản phẩm");
  }
}