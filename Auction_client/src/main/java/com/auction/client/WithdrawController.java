package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Class có nhiệm vụ quản lý màn hình rút tiền.
 */
public class WithdrawController {
  @FXML
  public void exit(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    stage.close();
  }
}
