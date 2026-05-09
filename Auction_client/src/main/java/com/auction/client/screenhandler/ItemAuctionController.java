package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.request.PlaceBidRequestDTO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class ItemAuctionController {
  @FXML
  private TextField bidAmountField;

  private String currentAuctionId;
  private HomeController homeController = new HomeController();

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
  public void placeBid() {
    try {
      String bidText = bidAmountField.getText();
      BigDecimal bidAmount = new BigDecimal(bidText);

      PlaceBidRequestDTO req =
          new PlaceBidRequestDTO(SessionManager.getCurrentAuctionId(),
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
