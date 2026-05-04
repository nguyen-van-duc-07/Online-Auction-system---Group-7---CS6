package com.auction.client.screenhandler;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Controller xử lý logic cho màn hình nạp tiền.
 */
public class DepositController {
  @FXML
  public void exit(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.close();
  }
}