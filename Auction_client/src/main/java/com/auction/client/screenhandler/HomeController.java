package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.request.*;
import com.auction.shared.response.AuctionResponseDTO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;

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
public class HomeController implements Initializable, ProductDetailNavigator {

  /** Biến static lưu trữ Controller hiện tại của Home. */
  private static HomeController instance;

  /**
   * Phương thức dùng để lấy ra instance của HomeController.
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

    // Gửi yêu cầu lấy danh sách ngay khi load UI
    ServerConnection.sendData(new GetActiveAndWaitingAuctionsRequestDTO());
  }

  /**
   * Tải và hiển thị danh sách thẻ sản phẩm (Component) lên giao diện.
   * Hàm này sẽ được ResponseHandler gọi sau khi nhận được dữ liệu từ Server.
   * * @param auctions Danh sách các phiên đấu giá trả về từ Server.
   */
  public void loadFeedToUI(List<AuctionDTO> auctions) {
    this.currentAuctions = auctions;
    // Bắt buộc dùng Platform.runLater để cập nhật UI an toàn từ luồng mạng (Network Thread)
    Platform.runLater(() -> {
      // Xóa các card cũ (nếu có) trước khi nạp mới
      feedContainer.getChildren().clear();

      for (AuctionDTO auction : auctions) {
        try {
          // 1. Khởi tạo FXMLLoader trỏ tới file thiết kế Component của KeDuc
          FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/Bidder/AuctionItemCard.fxml"));

          // 2. Load giao diện thành một Node (khối hình ảnh tĩnh)
          Node cardNode = loader.load();

          // 3. Lấy Controller quản lý Node đó ra để bơm dữ liệu vào
          AuctionItemCardController cardController = loader.getController();
          cardNode.setUserData(cardController);
          // Truyền object auction và 'this' (HomeController) sang để thẻ con biết đường gọi chuyển trang
          cardController.setData(auction, this);

          // 4. Nhét thẻ đã hoàn thiện vào FlowPane
          feedContainer.getChildren().add(cardNode);

        } catch (IOException e) {
          System.err.println("Lỗi khi load Component thẻ sản phẩm: " + e.getMessage());
          e.printStackTrace();
        }
      }
    });
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
  @Override
  public void gotoProductDetail(AuctionDTO selectedAuction) {
    // Lưu sản phẩm vừa chọn vào SessionManager
    SessionManager.setCurrentAuctionId(selectedAuction.getAuctionId());
    System.out.println("Đang mở chi tiết phiên đấu giá: " + selectedAuction.getAuctionId());
    ScreenController.switchScreen("Bidder/ItemAuction.fxml", "Phiên đấu giá " + selectedAuction.getItemName());
  }

  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Xác nhận đăng xuất",
        "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        LogoutRequestDTO logoutRequestDTO = new LogoutRequestDTO();
        logoutRequestDTO.setUserId(SessionManager.currentUser.getId());
        ServerConnection.sendData(logoutRequestDTO);
        SessionManager.clearSession();
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
}
