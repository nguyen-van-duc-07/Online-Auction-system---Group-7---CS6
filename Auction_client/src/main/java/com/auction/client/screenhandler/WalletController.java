package com.auction.client.screenhandler;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Class có nhiệm vụ quản lý màn hình ví người dùng.
 */
public class WalletController {
  HomeController homeController = new HomeController();

  @FXML
  public void gotoLogin() {
    homeController.gotoLogin();
  }

  @FXML
  public void gotoProfile() {
    homeController.gotoProfile();
  }

  @FXML
  public void gotoWithdraw(ActionEvent event) {
    ScreenController.createSubWindow(event, "Wallet/Withdraw.fxml", "Rút tiền");
  }

  @FXML
  public void gotoDeposit(ActionEvent event) {
    ScreenController.createSubWindow(event, "Wallet/Deposit.fxml", "Nạp tiền");
  }

  @FXML
  public void gotoResult() {
    homeController.gotoResult();
  }

  @FXML
  public void gotoHomeWithHyperLink() {
    ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
  }
}