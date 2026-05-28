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
 * Bộ điều khiển phụ quản lý giao diện chính dành cho người bán (SellerHome).
 * Quản lý danh sách sản phẩm đăng bán, thêm sản phẩm mới và gọi dịch vụ quản lý đơn hàng bán của mình.
 */
public class SellerHomeController {
  private static final Logger log = LoggerFactory.getLogger(SellerHomeController.class);

  private static SellerHomeController instance;

  private final MainLayoutController mainLayout;
  private List<AuctionDTO> currentAuctions = new ArrayList<>();
  private AuctionStatus currentStatusFilter = null;

  /**
   * Khởi tạo bộ điều khiển trang chủ người bán.
   *
   * @param mainLayout bộ điều khiển bố cục chính
   */
  public SellerHomeController(MainLayoutController mainLayout) {
    this.mainLayout = mainLayout;
    instance = this;
  }

  /**
   * Lấy instance duy nhất đang hoạt động của SellerHomeController.
   *
   * @return đối tượng SellerHomeController hiện tại
   */
  public static SellerHomeController getInstance() {
    return instance;
  }


  /**
   * Tải danh sách phiên đấu giá sản phẩm của người bán lên giao diện.
   *
   * @param auctions danh sách các phiên đấu giá của người bán
   */
  public void loadSellerFeedToUI(List<AuctionDTO> auctions) {
    this.currentAuctions = auctions;
    this.currentStatusFilter = null; // Reset filter khi load dữ liệu mới
    Platform.runLater(() -> {
      mainLayout.setFilterBarVisible(true); // Ensure filter bar is visible on seller feed
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
        cardController.setData(auction);

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
   * Lọc danh sách phiên đấu giá của người bán dựa trên từ khóa tìm kiếm.
   *
   * @param keyword từ khóa tìm kiếm (tên sản phẩm)
   */
  public void filterAuctions(String keyword) {
    applyFilters();
  }

  /**
   * Lọc các phiên đấu giá của seller theo trạng thái.
   *
   * @param status trạng thái của phiên đấu giá (hoặc null để lấy tất cả)
   */
  public void filterByStatus(AuctionStatus status) {
    this.currentStatusFilter = status;
    applyFilters();
  }

  private void applyFilters() {
    String keyword = mainLayout.getSearchField().getText().trim().toLowerCase();

    List<AuctionDTO> filtered = currentAuctions.stream()
        .filter(a -> keyword.isEmpty()
            || (a.getItemName() != null && a.getItemName().toLowerCase().contains(keyword)))
        .filter(a -> currentStatusFilter == null
            || a.getStatus() == currentStatusFilter)
        .toList();

    Platform.runLater(() -> loadSellerCards(filtered));
  }


  /**
   * Cập nhật mức giá cao nhất hiện tại của một phiên đấu giá trong danh sách.
   *
   * @param auctionId mã định danh phiên đấu giá cần cập nhật
   * @param newPrice mức giá mới nhất
   */
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

  /**
   * Gửi yêu cầu lên Server lấy danh sách các đơn hàng chờ người mua thanh toán của người bán.
   */
  public void handleGetPendingOrders() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetPendingOrdersOfSellerRequestDTO(userId));
  }

  /**
   * Gửi yêu cầu lên Server lấy danh sách các đơn hàng đã thanh toán thành công của người bán.
   */
  public void handleGetCompletedOrders() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetCompletedOrdersOfSellerRequestDTO(userId));
  }

  /**
   * Gửi yêu cầu lên Server lấy danh sách các đơn hàng bị hủy của người bán.
   */
  public void handleGetCanceledOrders() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetCancelledOrdersOfSellerRequestDTO(userId));
  }
}