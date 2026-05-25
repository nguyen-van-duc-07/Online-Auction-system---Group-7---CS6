package com.auction.client.screenhandler;

import com.auction.shared.model.order.OrderDTO;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import com.auction.shared.request.*;
import com.auction.client.network.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ResultController {
  private static final Logger log = LoggerFactory.getLogger(ResultController.class);

  private static ResultController instance;

  private final MainLayoutController mainLayout;
  private List<OrderDTO> orders;

  public ResultController(MainLayoutController mainLayout) {
    this.mainLayout = mainLayout;
    instance = this;
  }

  public static ResultController getInstance() {
    return instance;
  }

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
          cardController.setData(order, mainLayout);

          mainLayout.getFeedContainer().getChildren().add(cardNode);
        } catch (IOException e) {
          log.error("Lỗi khi load Component OrderCard", e);
        }
      }
    });
  }

  public void handleGetPendingOrders() {
    String userId = com.auction.client.network.SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetPendingOrdersOfBuyerRequestDTO(userId));
  }

  public void handleGetCompletedOrders() {
    String userId = com.auction.client.network.SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetCompletedOrdersOfBuyerRequestDTO(userId));
  }

  public void handleGetCancelledOrders() {
    String userId = com.auction.client.network.SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetCancelledOrdersOfBuyerRequestDTO(userId));
  }
}
