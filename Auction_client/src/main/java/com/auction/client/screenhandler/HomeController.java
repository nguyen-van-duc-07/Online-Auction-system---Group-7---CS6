package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.request.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.control.Button;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller xử lý logic cho màn hình trang chủ.
 * Đóng vai trò là Sub-controller cho MainLayoutController.
 */
public class HomeController {

  private static HomeController instance;

  private final MainLayoutController mainLayout;
  private List<AuctionDTO> currentAuctions = new ArrayList<>();

  public HomeController(MainLayoutController mainLayout) {
    this.mainLayout = mainLayout;
    instance = this;
  }

  public static HomeController getInstance() {
    return instance;
  }


  public void loadFeedToUI(List<AuctionDTO> auctions) {
    this.currentAuctions = auctions;
    Platform.runLater(() -> {
      // Đảm bảo ScrollPane đang hiển thị FlowPane feedContainer
      mainLayout.getMainContent().setContent(mainLayout.getFeedContainer());
      mainLayout.getMainContent().setFitToWidth(true);
      mainLayout.getMainContent().setFitToHeight(false);

      loadCards(auctions);
    });
  }

  private void loadCards(List<AuctionDTO> auctions) {
    mainLayout.getFeedContainer().getChildren().clear();
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
        e.printStackTrace();
      }
    }
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
  public void updateTimeExtend(String auctionId, LocalDateTime newEndTime) {
    for (AuctionDTO auction : currentAuctions) {
      if (auction.getAuctionId().equals(auctionId)) {
        auction.setEndTime(newEndTime);
        refreshAuctionTimeCard(auctionId, newEndTime);
        break;
      }
    }
  }
  private void refreshAuctionTimeCard(String auctionId, LocalDateTime newEndTime) {
    Platform.runLater(() -> {
      for (Node node : mainLayout.getFeedContainer().getChildren()) {
        AuctionItemCardController controller =
            (AuctionItemCardController) node.getUserData();

        if (controller != null && controller.getAuctionId().equals(auctionId)) {
          controller.updateEndTime(newEndTime); // 👈 quan trọng
          break;
        }
      }
    });
  }

  private void refreshAuctionCard(String auctionId, BigDecimal newPrice) {
    Platform.runLater(() -> {
      for (Node node : mainLayout.getFeedContainer().getChildren()) {
        AuctionItemCardController controller =
            (AuctionItemCardController) node.getUserData();
        if (controller != null && controller.getAuctionId().equals(auctionId)) {
          controller.updatePrice(newPrice);
          break;
        }
      }
    });
  }

  public void filterAuctions(String keyword) {
    List<AuctionDTO> filtered = keyword.isEmpty()
        ? currentAuctions
        : currentAuctions.stream()
        .filter(a -> a.getItemName().toLowerCase().contains(keyword))
        .toList();

    Platform.runLater(() -> {
      loadCards(filtered);
      if (filtered.isEmpty()) {
        Label noResult = new Label("Không tìm thấy sản phẩm nào!");
        noResult.setStyle("-fx-text-fill: #888; -fx-font-size: 14px; -fx-padding: 20;");
        mainLayout.getFeedContainer().getChildren().add(noResult);
      }
    });
  }

  public void handleGetActiveAuctions() {
    System.out.println(">>> Đã bấm nút Đang diễn ra");
    ServerConnection.sendData(new GetActiveAuctionsRequestDTO());
  }

  public void handleGetWaitingAuctions() {
    ServerConnection.sendData(new GetWaitingAuctionsRequestDTO());
  }

  public void handleGetClosedAuctions() {
    ServerConnection.sendData(new GetClosedAuctionsRequestDTO());
  }
}
