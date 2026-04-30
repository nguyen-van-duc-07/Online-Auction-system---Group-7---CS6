package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Class có nhiệm vụ quản lý màn hình ví người dùng.
 */
public class WalletController {
  HomeController homeController = new HomeController();

  @FXML
  public void gotoLogin(ActionEvent event) {
    homeController.gotoLogin(event);
  }

  @FXML
  public void gotoProfile(ActionEvent event) {
    homeController.gotoProfile(event);
  }

  @FXML
  public void gotoWithdraw(ActionEvent event) {
    ScreenController.creatSubWindow(event, "Withdraw.fxml", "Rút tiền");
  }

  @FXML
  public void gotoDeposit(ActionEvent event) {
    ScreenController.creatSubWindow(event, "Deposit.fxml", "Nạp tiền");
  }

  @FXML
  public void gotoResult(ActionEvent event) {
    homeController.gotoResult(event);
  }
}