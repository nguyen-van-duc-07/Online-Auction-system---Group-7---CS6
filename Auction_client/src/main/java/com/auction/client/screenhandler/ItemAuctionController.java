package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.request.JoinRoomRequestDTO;
import com.auction.shared.request.LeaveRoomRequestDTO;
import com.auction.shared.request.PlaceBidRequestDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class ItemAuctionController implements Initializable {
  @FXML
  private TextField bidAmountField;
  @FXML
  private Label itemNameLabel;
  @FXML
  private Label descriptionField;
  @FXML
  private Label currentPriceField;

  private HomeController homeController = new HomeController();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ServerConnection.sendData(new JoinRoomRequestDTO(SessionManager.getCurrentAuction().getId()));
    itemNameLabel.setText(SessionManager.getCurrentAuction().getItem().getName());
    descriptionField.setText(SessionManager.getCurrentAuction().getItem().getDescription());
    currentPriceField.setText("Giá hiện tại: "
        + SessionManager.getCurrentAuction().getCurrentHighestPrice().toString());
  }

  public void leaveCurrentAuction() {
    ServerConnection.sendData(new LeaveRoomRequestDTO(SessionManager.getCurrentAuction().getId()));
  }
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

  @FXML
  public void gotoSellerHome() {
    homeController.gotoSellerHome();
  }

  @FXML
  public void placeBid() {
    try {
      String bidText = bidAmountField.getText();
      BigDecimal bidAmount = new BigDecimal(bidText);

      PlaceBidRequestDTO req =
          new PlaceBidRequestDTO(SessionManager.getCurrentAuction().getId(),
                                 SessionManager.getCurrentUser().getId(),
                                 bidAmount);
      System.out.println(
          "AUCTION ID = "
              + req.getAuctionId()
      );
      System.out.println(
          "BIDDER ID = "
              + req.getBidderId()
      );
      System.out.println(
          "BID AMOUNT = "
              + req.getBidAmount()
      );
      ServerConnection.sendData(req);
      System.out.println(
          "BID SENT"
      );

    } catch (Exception e) {

      e.printStackTrace();
    }
  }
}
