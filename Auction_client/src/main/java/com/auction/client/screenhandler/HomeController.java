package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.enums.ItemType;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.request.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller xử lý logic cho màn hình trang chủ.
 * Đóng vai trò là Sub-controller cho MainLayoutController.
 */
public class HomeController {
  private static final Logger log = LoggerFactory.getLogger(HomeController.class);

  private static HomeController instance;

  private final MainLayoutController mainLayout;
  private List<AuctionDTO> currentAuctions = new ArrayList<>();
  private ItemType currentCategoryFilter = null;

  /**
   * Khởi tạo bộ điều khiển trang chủ.
   *
   * @param mainLayout bộ điều khiển bố cục chính
   */
  public HomeController(MainLayoutController mainLayout) {
    this.mainLayout = mainLayout;
    instance = this;
  }

  /**
   * Lấy instance duy nhất đang hoạt động của HomeController.
   *
   * @return đối tượng HomeController hiện tại
   */
  public static HomeController getInstance() {
    return instance;
  }


  /**
   * Tải danh sách phiên đấu giá lên giao diện của trang chủ.
   *
   * @param auctions danh sách các phiên đấu giá (AuctionDTO)
   */
  public void loadFeedToUI(List<AuctionDTO> auctions) {
    this.currentAuctions = auctions;
    Platform.runLater(() -> {
      mainLayout.setFilterBarVisible(true); // Ensure filter bar is visible on home feed
      // Đảm bảo ScrollPane đang hiển thị FlowPane feedContainer
      mainLayout.getMainContent().setContent(mainLayout.getFeedContainer());
      mainLayout.getMainContent().setFitToWidth(true);
      mainLayout.getMainContent().setFitToHeight(false);

      loadCards(auctions);

      if (auctions == null || auctions.isEmpty()) {
        mainLayout.showPlaceholder("Không tìm thấy phiên đấu giá nào");
      } else {
        mainLayout.hidePlaceholder();
      }
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
        cardController.setData(auction);
        mainLayout.getFeedContainer().getChildren().add(cardNode);
      } catch (IOException e) {
        log.error("Lỗi khi tải thẻ card đấu giá cho phiên ID: {}", auction.getAuctionId(), e);
      }
    }
  }

  /**
   * Cập nhật mức giá cao nhất mới cho một phiên đấu giá trong danh sách.
   *
   * @param auctionId mã định danh phiên đấu giá cần cập nhật
   * @param newPrice mức giá mới
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
  /**
   * Cập nhật thời điểm kết thúc mới của phiên đấu giá khi có gia hạn.
   *
   * @param auctionId mã định danh phiên đấu giá cần cập nhật
   * @param newEndTime thời điểm kết thúc mới
   */
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

  /**
   * Lọc danh sách các phiên đấu giá theo từ khóa tìm kiếm.
   *
   * @param keyword từ khóa tìm kiếm
   */
  public void filterAuctions(String keyword) {
    applyFilters();
  }

  /**
   * Lọc theo loại sản phẩm (null = tất cả).
   */
  /**
   * Lọc danh sách các phiên đấu giá theo loại sản phẩm.
   *
   * @param type loại sản phẩm sản phẩm
   */
  public void filterByCategory(ItemType type) {
    this.currentCategoryFilter = type;
    applyFilters();
  }

  /**
   * Áp dụng tổng hợp cả search text và category filter.
   */
  private void applyFilters() {
    String keyword = mainLayout.getSearchField().getText().trim().toLowerCase();

    List<AuctionDTO> filtered = currentAuctions.stream()
        .filter(a -> keyword.isEmpty()
            || a.getItemName().toLowerCase().contains(keyword))
        .filter(a -> currentCategoryFilter == null
            || a.getItemType() == currentCategoryFilter)
        .toList();

    Platform.runLater(() -> {
      loadCards(filtered);
      if (filtered.isEmpty()) {
        mainLayout.showPlaceholder("Không tìm thấy phiên đấu giá nào");
      } else {
        mainLayout.hidePlaceholder();
      }
    });
  }

  /**
   * Yêu cầu hệ thống tải các phiên đấu giá đang diễn ra từ Server.
   */
  public void handleGetActiveAuctions() {
    log.info(">>> Đã bấm nút Đang diễn ra");
    ServerConnection.sendData(new GetAuctionsRequestDTO(AuctionStatus.ACTIVE));
  }

  /**
   * Yêu cầu hệ thống tải các phiên đấu giá sắp diễn ra từ Server.
   */
  public void handleGetWaitingAuctions() {
    ServerConnection.sendData(new GetAuctionsRequestDTO(AuctionStatus.WAITING));
  }

  /**
   * Yêu cầu hệ thống tải các phiên đấu giá đã kết thúc từ Server.
   */
  public void handleGetClosedAuctions() {
    ServerConnection.sendData(new GetAuctionsRequestDTO(AuctionStatus.CLOSED));
  }
}
