package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.enums.AuctionStatus;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class có nhiệm vụ quản lý màn hình seller.
 * Đóng vai trò là Sub-controller cho MainLayoutController.
 */
public class SellerHomeController {
  private static final Logger log = LoggerFactory.getLogger(SellerHomeController.class);

  private static SellerHomeController instance;

  private final MainLayoutController mainLayout;
  private List<AuctionDTO> currentAuctions = new ArrayList<>();
  private AuctionStatus currentStatusFilter = null;

  public SellerHomeController(MainLayoutController mainLayout) {
    this.mainLayout = mainLayout;
    instance = this;
  }

  public static SellerHomeController getInstance() {
    return instance;
  }


  public void loadSellerFeedToUI(List<AuctionDTO> auctions) {
    this.currentAuctions = auctions;
    this.currentStatusFilter = null; // Reset filter khi load dữ liệu mới
    Platform.runLater(() -> {
      mainLayout.getMainContent().setContent(mainLayout.getFeedContainer());
      mainLayout.getMainContent().setFitToWidth(true);
      mainLayout.getMainContent().setFitToHeight(false);

      loadSellerCards(auctions);
    });
  }

  /**
   * Render các card sản phẩm của seller vào feedContainer.
   * Tách riêng để có thể gọi lại khi filter.
   */
  private void loadSellerCards(List<AuctionDTO> auctions) {
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
        log.error("Lỗi khi load Component thẻ sản phẩm", e);
      }
    }

    if (auctions.isEmpty()) {
      Label noResult = new Label("Không tìm thấy phiên đấu giá nào!");
      noResult.setStyle("-fx-text-fill: #888; -fx-font-size: 14px; -fx-padding: 20;");
      mainLayout.getFeedContainer().getChildren().add(noResult);
    }
  }

  /**
   * Lọc các phiên đấu giá của seller theo trạng thái.
   * @param status null = tất cả
   */
  public void filterByStatus(AuctionStatus status) {
    this.currentStatusFilter = status;
    List<AuctionDTO> filtered = (status == null)
        ? currentAuctions
        : currentAuctions.stream()
            .filter(a -> a.getStatus() == status)
            .toList();
    Platform.runLater(() -> loadSellerCards(filtered));
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