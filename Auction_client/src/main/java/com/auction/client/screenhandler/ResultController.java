package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.request.GetOrdersOfBuyerRequestDTO;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Bộ điều khiển (Controller) phụ quản lý kết quả đấu giá các đơn hàng của người mua.
 * Chịu trách nhiệm nạp danh sách đơn hàng, lọc tìm kiếm đơn hàng và gọi dịch vụ tải dữ liệu từ Server.
 */
public class ResultController {
  private static final Logger log = LoggerFactory.getLogger(ResultController.class);

  private static ResultController instance;

  private final MainLayoutController mainLayout;
  private List<OrderDTO> orders;

  /**
   * Khởi tạo bộ điều khiển kết quả đơn hàng.
   *
   * @param mainLayout bộ điều khiển bố cục chính
   */
  public ResultController(MainLayoutController mainLayout) {
    this.mainLayout = mainLayout;
    instance = this;
  }

  /**
   * Lấy instance duy nhất đang hoạt động của ResultController.
   *
   * @return đối tượng ResultController hiện tại
   */
  public static ResultController getInstance() {
    return instance;
  }

  /**
   * Tải danh sách đơn hàng lên vùng hiển thị nội dung chính.
   *
   * @param orders danh sách đơn hàng cần nạp
   */
  public void loadOrdersToUI(List<OrderDTO> orders) {
    this.orders = orders;
    Platform.runLater(() -> {
      mainLayout.getMainContent().setContent(mainLayout.getFeedContainer());
      mainLayout.getMainContent().setFitToWidth(true);
      mainLayout.getMainContent().setFitToHeight(false);

      mainLayout.getFeedContainer().getChildren().clear();

      for (OrderDTO order : orders) {
        try {
          FXMLLoader loader = new FXMLLoader(
              getClass().getResource("/com/auction/client/User/OrderCard.fxml"));
          Node cardNode = loader.load();

          OrderCardController cardController = loader.getController();
          cardNode.setUserData(cardController);
          cardController.setData(order);

          mainLayout.getFeedContainer().getChildren().add(cardNode);
        } catch (IOException e) {
          log.error("Lỗi khi load Component OrderCard", e);
        }
      }

      if (orders == null || orders.isEmpty()) {
        mainLayout.showPlaceholder("Không tìm thấy đơn hàng nào");
      } else {
        mainLayout.hidePlaceholder();
      }
    });
  }

  /**
   * Lọc danh sách các đơn hàng hiện có dựa trên từ khóa tìm kiếm.
   *
   * @param keyword từ khóa tìm kiếm (tên sản phẩm hoặc thương hiệu)
   */
  public void filterOrders(String keyword) {
    if (orders == null) return;

    List<OrderDTO> filtered = orders.stream()
        .filter(o -> keyword.isEmpty()
            || (o.getItemName() != null && o.getItemName().toLowerCase().contains(keyword))
            || (o.getBrandName() != null && o.getBrandName().toLowerCase().contains(keyword)))
        .toList();

    Platform.runLater(() -> {
      mainLayout.getFeedContainer().getChildren().clear();
      for (OrderDTO order : filtered) {
        try {
          FXMLLoader loader = new FXMLLoader(
              getClass().getResource("/com/auction/client/User/OrderCard.fxml"));
          Node cardNode = loader.load();

          OrderCardController cardController = loader.getController();
          cardNode.setUserData(cardController);
          cardController.setData(order);

          mainLayout.getFeedContainer().getChildren().add(cardNode);
        } catch (IOException e) {
          log.error("Lỗi khi load Component OrderCard trong lúc tìm kiếm", e);
        }
      }

      if (filtered.isEmpty()) {
        mainLayout.showPlaceholder("Không tìm thấy đơn hàng nào");
      } else {
        mainLayout.hidePlaceholder();
      }
    });
  }

  /**
   * Gửi yêu cầu lên Server lấy danh sách các đơn hàng đang chờ thanh toán của người mua.
   */
  public void handleGetPendingOrders() {
    String userId = com.auction.client.network.SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetOrdersOfBuyerRequestDTO(userId, OrderStatus.PENDING));
  }

  /**
   * Gửi yêu cầu lên Server lấy danh sách các đơn hàng đã thanh toán thành công của người mua.
   */
  public void handleGetConfirmedOrders() {
    String userId = com.auction.client.network.SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetOrdersOfBuyerRequestDTO(userId, OrderStatus.CONFIRMED));
  }

  /**
   * Gửi yêu cầu lên Server lấy danh sách các đơn hàng đã bị hủy của người mua.
   */
  public void handleGetCancelledOrders() {
    String userId = com.auction.client.network.SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetOrdersOfBuyerRequestDTO(userId, OrderStatus.CANCELLED));
  }
}
