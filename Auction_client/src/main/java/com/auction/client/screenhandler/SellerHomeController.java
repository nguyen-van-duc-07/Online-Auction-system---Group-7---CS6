package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.request.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class có nhiệm vụ quản lý màn hình seller.
 * Đóng vai trò là Sub-controller cho MainLayoutController.
 */
public class SellerHomeController {
  private static SellerHomeController instance;

  private final MainLayoutController mainLayout;
  private List<AuctionDTO> currentAuctions = new ArrayList<>();

  public SellerHomeController(MainLayoutController mainLayout) {
    this.mainLayout = mainLayout;
    instance = this;
  }

  public static SellerHomeController getInstance() {
    return instance;
  }


  public void loadSellerFeedToUI(List<AuctionDTO> auctions) {
    this.currentAuctions = auctions;
    Platform.runLater(() -> {
      mainLayout.getMainContent().setContent(mainLayout.getFeedContainer());
      mainLayout.getMainContent().setFitToWidth(true);
      mainLayout.getMainContent().setFitToHeight(false);

      mainLayout.getFeedContainer().getChildren().clear();

      // === Card "Đăng bán sản phẩm" ===
      VBox addNewCard = new VBox(5);
      addNewCard.setPrefSize(760, 110);
      addNewCard.setAlignment(Pos.CENTER);

      String normalStyle = "-fx-background-color: #27ae60; -fx-border-color: #219653; "
          + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;";
      String hoverStyle = "-fx-background-color: #2ecc71; -fx-border-color: #27ae60; "
          + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;";

      addNewCard.setStyle(normalStyle);
      addNewCard.setOnMouseEntered(e -> addNewCard.setStyle(hoverStyle));
      addNewCard.setOnMouseExited(e -> addNewCard.setStyle(normalStyle));

      Label plusIcon = new Label("+");
      plusIcon.setStyle("-fx-font-size: 60px; -fx-text-fill: white; -fx-font-weight: bold;");

      Label textLabel = new Label("Đăng bán sản phẩm");
      textLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

      addNewCard.getChildren().addAll(plusIcon, textLabel);
      addNewCard.setOnMouseClicked(event -> mainLayout.gotoUploadItem());

      mainLayout.getFeedContainer().getChildren().add(addNewCard);

      // === Các thẻ auction của seller ===
      for (AuctionDTO auction : auctions) {
        try {
          FXMLLoader loader = new FXMLLoader(
              getClass().getResource("/com/auction/client/Bidder/AuctionItemCard.fxml"));
          Node cardNode = loader.load();

          AuctionItemCardController cardController = loader.getController();
          cardNode.setUserData(cardController);
          cardController.setData(auction, mainLayout);

          mainLayout.getFeedContainer().getChildren().add(cardNode);
        } catch (IOException e) {
          System.err.println("Lỗi khi load Component thẻ sản phẩm: " + e.getMessage());
          e.printStackTrace();
        }
      }
    });
  }

  public void updateAuctionPrice(String auctionId, BigDecimal newPrice) {
    for (AuctionDTO auction : currentAuctions) {
      if (auction.getAuctionId().equals(auctionId)) {
        auction.setCurrentPrice(newPrice);
        refreshAuctionCard(auctionId, newPrice);
        break;
      }
    }
  }

  private void refreshAuctionCard(String auctionId, BigDecimal newPrice) {
    Platform.runLater(() -> {
      for (Node node : mainLayout.getFeedContainer().getChildren()) {
        if (node.getUserData() instanceof AuctionItemCardController controller) {
          if (controller.getAuctionId() != null && controller.getAuctionId().equals(auctionId)) {
            controller.updatePrice(newPrice);
            break;
          }
        }
      }
    });
  }

  public void handleGetPendingOrders() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetPendingOrdersOfSellerRequestDTO(userId));
  }

  public void handleGetCompletedOrders() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetCompletedOrdersOfSellerRequestDTO(userId));
  }

  public void handleGetCanceledOrders() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetCancelledOrdersOfSellerRequestDTO(userId));
  }
}