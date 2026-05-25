package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.request.AuctionRequestDTO;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller xử lý logic cho màn hình kết quả đấu giá.
 */
public class OrderCardController {
  private static final Logger log = LoggerFactory.getLogger(OrderCardController.class);
  @FXML
  private Label itemNameLabel;

  @FXML
  private Label finalPriceLabel;

  @FXML
  private Label brandNameLabel;

  @FXML
  private Label winnerNameLabel;

  private Controller currentScreen;
  private OrderDTO currentOrder;

  @FXML
  public void gotoPayment() {
    SessionManager.setCurrentOrderId(currentOrder.getOrderId());
    SessionManager.setPreviousScreen(currentScreen);
    log.info("Đang mở chi tiết phiên đơn hàng: {}", currentOrder.getAuctionId());
    ScreenController.switchScreen("Bidder/PaymentScreen.fxml", "Đơn hàng " + currentOrder.getItemName());
    if (PaymentScreenController.instance != null) {
      PaymentScreenController.instance.setOrderData(currentOrder);
    }
  }

  @FXML
  public void gotoItemDetail() {
    String auctionId = currentOrder.getAuctionId();
    AuctionRequestDTO auctionRequestDTO = new AuctionRequestDTO();
    auctionRequestDTO.setAuctionId(auctionId);
    ServerConnection.sendData(auctionRequestDTO);
  }

  /**
   * Hàm này dùng để bơm dữ liệu từ một Controller cha truyền sang.
   *
   */
  public void setData(OrderDTO order, Controller currentScreen) {
    this.currentScreen = currentScreen;
    this.currentOrder = order;
    // Đổ dữ liệu text
    itemNameLabel.setText(order.getItemName());

    String formattedPrice = String.format("%,.0f VNĐ", order.getFinalPrice());
    finalPriceLabel.setText(formattedPrice);

    brandNameLabel.setText(order.getBrandName());
    winnerNameLabel.setText(order.getWinnerName());
  }
}
