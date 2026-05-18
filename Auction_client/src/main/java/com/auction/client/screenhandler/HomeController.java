package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.request.GetActiveAuctionRequestDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller xử lý logic cho màn hình trang chủ mới.
 * Chịu trách nhiệm hiển thị danh sách sản phẩm đấu giá động từ Server
 * và điều hướng các chức năng quản lý, lọc trạng thái.
 */
public class HomeController implements Initializable, ProductDetailNavigator {

  private static HomeController instance;

  public static HomeController getInstance() {
    return instance;
  }

  private List<AuctionResponseDTO> currentAuctions = new ArrayList<>();

  /** Khung chứa các thẻ sản phẩm (Nằm trong ScrollPane ở giữa màn hình) */
  @FXML
  private FlowPane feedContainer;

  /** Thanh tìm kiếm ở Header */
  @FXML
  private TextField searchField;

  private double mouseAnchorX;
  private double mouseAnchorY;


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;
    // Gửi yêu cầu lấy danh sách (mặc định lấy Đang diễn ra) ngay khi load UI
    ServerConnection.sendData(new GetActiveAuctionRequestDTO());
  }

  public void loadFeedToUI(List<AuctionResponseDTO> auctions) {
    this.currentAuctions = auctions;
    Platform.runLater(() -> {
      feedContainer.getChildren().clear();

      for (AuctionResponseDTO auction : auctions) {
        try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/Bidder/AuctionItemCard.fxml"));
          Node cardNode = loader.load();

          AuctionItemCardController cardController = loader.getController();
          cardNode.setUserData(cardController);
          cardController.setData(auction, this);

          feedContainer.getChildren().add(cardNode);
        } catch (IOException e) {
          System.err.println("Lỗi khi load Component thẻ sản phẩm: " + e.getMessage());
          e.printStackTrace();
        }
      }
    });
  }

  public void updateAuctionPrice(String auctionId, BigDecimal newPrice) {
    for (AuctionResponseDTO auction : currentAuctions) {
      if (auction.getId().equals(auctionId)) {
        auction.setCurrentHighestPrice(newPrice);
        refreshAuctionCard(auctionId, newPrice);
        break;
      }
    }
  }

  private void refreshAuctionCard(String auctionId, BigDecimal newPrice) {
    Platform.runLater(() -> {
      for (Node node : feedContainer.getChildren()) {
        AuctionItemCardController controller = (AuctionItemCardController) node.getUserData();
        if (controller != null && controller.getAuctionId().equals(auctionId)) {
          controller.updatePrice(newPrice);
          break;
        }
      }
    });
  }

  @Override
  public void gotoProductDetail(AuctionResponseDTO selectedAuction) {
    SessionManager.setCurrentAuction(selectedAuction);
    System.out.println("Đang mở chi tiết phiên đấu giá: " + selectedAuction.getId());
    ScreenController.switchScreen("Bidder/ItemAuction.fxml", "Phiên đấu giá " + selectedAuction.getItem().getName());
  }

  /** Xử lý tìm kiếm khi người dùng Enter hoặc click nút Search */
  @FXML
  public void handleSearch() {
    String keyword = searchField.getText().trim();
    System.out.println("Đang tìm kiếm: " + keyword);
    // TODO: Gửi Request tìm kiếm lên Server hoặc filter list currentAuctions local
  }

  /** Xử lý click vào chuông thông báo góc dưới phải */
  @FXML
  public void handleNotifications() {
    System.out.println("Mở popup/panel thông báo...");
    // TODO: Hiển thị giao diện thông báo
  }
  /** Ghi lại vị trí chuột ngay khi người dùng vừa nhấn giữ vào quả chuông */
  @FXML
  public void onIconPressed(javafx.scene.input.MouseEvent event) {
    javafx.scene.Node node = (javafx.scene.Node) event.getSource();
    mouseAnchorX = event.getSceneX() - node.getTranslateX();
    mouseAnchorY = event.getSceneY() - node.getTranslateY();
  }
  /** Cập nhật vị trí mới của quả chuông liên tục theo đường di chuyển của chuột */
  @FXML
  public void onIconDragged(javafx.scene.input.MouseEvent event) {
    javafx.scene.Node node = (javafx.scene.Node) event.getSource();
    node.setTranslateX(event.getSceneX() - mouseAnchorX);
    node.setTranslateY(event.getSceneY() - mouseAnchorY);
  }

  // --- CÁC NÚT Ở SIDEBAR BÊN TRÁI ---

  @FXML
  public void gotoSellerHome() {
    ScreenController.navigateToSellerChannel();
  }

  @FXML
  public void gotoResult() {
    ScreenController.switchScreen("Bidder/Result.fxml", "Kết quả đấu giá");
  }

  @FXML
  public void filterOngoingAuctions() {
    System.out.println("Lọc phiên: Đang diễn ra");
    // TODO: Gửi request lấy phiên "Đang diễn ra" (Active) hoặc đổi style của nút để báo hiệu đang chọn
    ServerConnection.sendData(new GetActiveAuctionRequestDTO());
  }

  @FXML
  public void filterUpcomingAuctions() {
    System.out.println("Lọc phiên: Sắp diễn ra");
    // TODO: Gửi request lấy phiên "Sắp diễn ra" (Upcoming)
  }

  @FXML
  public void filterEndedAuctions() {
    System.out.println("Lọc phiên: Đã kết thúc");
    // TODO: Gửi request lấy phiên "Đã kết thúc" (Completed/Ended)
  }

  // --- CÁC NÚT TRONG DROPDOWN "TÀI KHOẢN" ---

  @FXML
  public void gotoProfile() {
    ScreenController.switchScreen("User/Profile.fxml", "Thông tin tài khoản");
  }

  @FXML
  public void gotoWallet() {
    ScreenController.switchScreen("User/Wallet/Wallet.fxml", "Ví người dùng");
  }

  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Xác nhận đăng xuất",
            "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(response -> {
      if (response == ButtonType.OK) {
        SessionManager.clearSession();
        ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");
      }
    });
  }
}