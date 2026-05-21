package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.request.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller xử lý logic cho màn hình trang chủ.
 * Chịu trách nhiệm hiển thị danh sách sản phâ đấu giá động từ Server.
 */
public class HomeController implements Initializable, Controller {

  /**
   * Biến static lưu trữ Controller hiện tại của Home.
   */
  private static HomeController instance;

  /**
   * Phương thức dùng để lấy ra instance của HomeController.
   *
   * @return đối tượng kiểu HomeController
   */
  public static HomeController getInstance() {
    return instance;
  }

  // THÊM field này
  private List<AuctionDTO> currentAuctions = new ArrayList<>();

  @FXML
  private ScrollPane mainContent;

  @FXML
  private FlowPane feedContainer;

  @FXML
  private Node homeFeedNode;

  @FXML
  private Label realNameLabel;

  @FXML
  private Label notificationBadge;

  @FXML
  private TextField searchField;

  @FXML
  private Label remainingLabel;

  /**
   * Phương thức khởi tạo mặc định của JavaFX (thuộc interface Initializable).
   *
   * <p>Được tự động gọi ngay sau khi file FXML của màn hình Home được tải lên thành công
   * và các thành phần giao diện (UI components) đã được ánh xạ.
   * Tại đây, hệ thống sẽ tự động gửi một {@link GetActiveAuctionsRequestDTO}
   * qua Socket lên Server để xin danh sách sản phẩm hiển thị lên bảng tin (Feed)
   * mà không cần người dùng phải bấm nút tải lại.</p>
   *
   * @param location  Đường dẫn tương đối (URL) tới file FXML.
   * @param resources Các tài nguyên bản địa hóa (nếu có).
   */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Ghi nhận bản thân (this) làm instance hiện tại ngay khi màn hình vừa mở lên
    instance = this;

    // Lưu lại giao diện Node gốc của trang chủ
    homeFeedNode = mainContent.getContent();

    loadUnreadCount();

    // Hiện Label chào user
    String phoneNumber = SessionManager.currentUser.getPhoneNumber();
    String realName = SessionManager.currentUser.getRealName();
    if (realName != null) {
      realNameLabel.setText("Chào, " + realName);
    } else if (phoneNumber != null) {
      realNameLabel.setText("Chào, " + phoneNumber);
    } else {
      realNameLabel.setText("N/A");
    }

    // Hiển thị số dư lần đầu tiên khi vừa load màn hình (Tránh việc nhãn bị trống)
    if (SessionManager.getCurrentBalance() != null) {
      java.text.NumberFormat format = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
      remainingLabel.setText("Số dư: " + format.format(SessionManager.getCurrentBalance()) + " VNĐ");
    }

    //  Đăng ký Listener: Từ nay về sau, hễ SessionManager có số mới là UI tự update
    SessionManager.balanceProperty().addListener((observable, oldBalance, newBalance) -> {
      Platform.runLater(() -> {
        if (remainingLabel != null && newBalance != null) {
          java.text.NumberFormat format = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
          remainingLabel.setText("Số dư: " + format.format(newBalance) + " VNĐ");
        }
      });
    });

    // Gửi yêu cầu lấy danh sách ngay khi load UI
    ServerConnection.sendData(new GetActiveAndWaitingAuctionsRequestDTO());
    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
      filterAuctions(newValue.trim().toLowerCase());
    });
  }
  private void loadCards(List<AuctionDTO> auctions) {
    feedContainer.getChildren().clear();
    for (AuctionDTO auction : auctions) {
      try {
        // 1. Khởi tạo FXMLLoader trỏ tới file thiết kế Component của KeDuc
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/auction/client/Bidder/AuctionItemCard.fxml")
        );
        // 2. Load giao diện thành một Node (khối hình ảnh tĩnh)
        Node cardNode = loader.load();
        // 3. Lấy Controller quản lý Node đó ra để bơm dữ liệu vào
        AuctionItemCardController cardController = loader.getController();
        cardNode.setUserData(cardController);
        // Truyền object auction
        cardController.setData(auction, instance);
        // 4. Nhét thẻ đã hoàn thiện vào FlowPane
        feedContainer.getChildren().add(cardNode);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Tải và hiển thị danh sách thẻ sản phẩm (Component) lên giao diện.
   * Hàm này sẽ được ResponseHandler gọi sau khi nhận được dữ liệu từ Server.
   * * @param auctions Danh sách các phiên đấu giá trả về từ Server.
   */
  public void loadFeedToUI(List<AuctionDTO> auctions) {
    this.currentAuctions = auctions;
    Platform.runLater(() -> loadCards(auctions));
  }


  public void updateAuctionPrice(String auctionId, BigDecimal newPrice) {
    // Tìm auction trong danh sách hiện tại và cập nhật giá
    for (AuctionDTO auction : currentAuctions) {
      if (auction.getAuctionId().equals(auctionId)) {
        auction.setCurrentPrice(newPrice);
        // Cập nhật UI của card tương ứng
        refreshAuctionCard(auctionId, newPrice);
        break;
      }
    }
  }

  private void refreshAuctionCard(String auctionId, BigDecimal newPrice) {
    Platform.runLater(() -> {
      for (Node node : feedContainer.getChildren()) {
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
   * Chuyển hướng sang màn hình chi tiết sản phẩm.
   */

  private void loadUnreadCount() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetNotificationsRequestDTO(userId));
  }

  public void updateNotificationBadge(int unreadCount) {
    Platform.runLater(() -> {
      if (unreadCount > 0) {
        notificationBadge.setVisible(true);
        // Chỉ hiện chấm đỏ, không hiện số
        notificationBadge.setText("");
      } else {
        notificationBadge.setVisible(false);
      }
    });
  }

  public void incrementNotificationBadge() {
    Platform.runLater(() -> notificationBadge.setVisible(true));
  }

  @FXML
  public void gotoNotifications() {
    ScreenController.createSubWindow("Bidder/Notifications.fxml", "Thông báo");
    // Load danh sách khi mở
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetNotificationsRequestDTO(userId));
  }

  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Xác nhận đăng xuất",
        "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        LogoutRequestDTO logoutRequestDTO = new LogoutRequestDTO();
        logoutRequestDTO.setUserId(SessionManager.currentUser.getId());
        ServerConnection.sendData(logoutRequestDTO);
        SessionManager.setCurrentUser(null);
        ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");
        ScreenController.primaryStage.setMaximized(false);
      }
    });
  }

  @FXML
  public void gotoSellerHome() {
    // Gửi yêu cầu kiểm tra hồ sơ người bán lên Server
    String userId = SessionManager.currentUser.getId();
    CheckingSellerProfileRequestDTO request = new CheckingSellerProfileRequestDTO(userId);
    ServerConnection.sendData(request);
  }

  @FXML
  public void gotoProfile() {
    loadComponent("/com/auction/client/User/Profile.fxml");
  }

  @FXML
  public void gotoWallet() {
    loadComponent(("/com/auction/client/User/Wallet/Wallet.fxml"));
  }

  @FXML
  public void gotoResult() {
    ScreenController.switchScreen("Bidder/Result.fxml", "Kết quả đấu giá");
  }

  @FXML
  public void gotoHomeFeed() {
    // Nếu đã lưu giao diện Feed, chỉ cần set lại nó vào phần mainContent
    if (homeFeedNode != null) {
      mainContent.setContent(homeFeedNode);

      /* Gửi lại request lên Server để cập nhật danh sách đấu giá mới nhất
         mỗi khi người dùng bấm về trang chủ*/
      ServerConnection.sendData(new GetActiveAndWaitingAuctionsRequestDTO());
    }
  }

  @FXML
  public void handleGetActiveAuctions() {
    System.out.println(">>> Đã bấm nút Đang diễn ra");
    ServerConnection.sendData(new GetActiveAuctionsRequestDTO());
  }

  @FXML
  public void handleGetWaitingAuctions() {
    ServerConnection.sendData(new GetWaitingAuctionsRequestDTO());
  }

  @FXML
  public void handleGetClosedAuctions() {
    ServerConnection.sendData(new GetClosedAuctionsRequestDTO());
  }

  /**
   * Nạp file FXML và thay thế toàn bộ nội dung hiện tại của ScrollPane.
   */
  public void loadComponent(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent newNode = loader.load();

      mainContent.setContent(newNode);

      mainContent.setFitToHeight(true);
      mainContent.setFitToWidth(true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void handleGetMyActiveAuctions(ActionEvent actionEvent) {
  }

  public void handleGetPendingOrders(ActionEvent actionEvent) {
  }

  public void handleGetCompletedOrders(ActionEvent actionEvent) {
  }

  public void handleGetCanceledOrders(ActionEvent actionEvent) {

  }
  private void filterAuctions(String keyword) {
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
        feedContainer.getChildren().add(noResult);
      }
    });
  }
}
