package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.request.GetPendingOrdersOfBuyerRequestDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ResultController implements Initializable {

  @FXML
  private ScrollPane mainContent;

  @FXML
  private VBox feedContainer;

  private List<OrderDTO> orders;
  private static ResultController instance;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;
    ServerConnection.sendData(new GetPendingOrdersOfBuyerRequestDTO(
        SessionManager.getCurrentUser().getId()));
  }

  /**
   * Tải và hiển thị danh sách thẻ sản phẩm (Component) lên giao diện.
   * Hàm này sẽ được ResponseHandler gọi sau khi nhận được dữ liệu từ Server.
   * * @param auctions Danh sách các phiên đấu giá trả về từ Server.
   */
  public void loadFeedToUI(List<OrderDTO> orders) {
    this.orders = orders;
    Platform.runLater(() -> {
      // Xóa các card cũ (nếu có) trước khi nạp mới
      feedContainer.getChildren().clear();

      for (OrderDTO order : orders) {
        try {
          // 1. Khởi tạo FXMLLoader trỏ tới file thiết kế Card
          FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/User/OrderCard.fxml"));

          // 2. Load giao diện thành một Node (khối hình ảnh tĩnh)
          Node cardNode = loader.load();

          // 3. Lấy Controller quản lý Node đó ra để bơm dữ liệu vào
          OrderCardController cardController = loader.getController();
          cardNode.setUserData(cardController);
          // Truyền object auction
          cardController.setData(order, new HomeController());

          // 4. Nhét thẻ đã hoàn thiện vào VBox
          feedContainer.getChildren().add(cardNode);

        } catch (IOException e) {
          System.err.println("Lỗi khi load Component thẻ sản phẩm: " + e.getMessage());
          e.printStackTrace();
        }
      }
    });
  }

  public static ResultController getInstance() {
    return instance;
  }
}
