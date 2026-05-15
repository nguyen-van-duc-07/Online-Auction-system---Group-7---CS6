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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
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


  public static HomeController getInstance() {
    return instance;
  }
  // THÊM field này
  private List<AuctionResponseDTO> currentAuctions = new ArrayList<>();
  /** Khung chứa các thẻ sản phẩm, được ánh xạ từ fx:id="feedContainer" trong Bidder/Home.fxml. */
  @FXML
  private FlowPane feedContainer;

  /**
   * Phương thức khởi tạo mặc định của JavaFX (thuộc interface Initializable).
   *
   * <p>Được tự động gọi ngay sau khi file FXML của màn hình Home được tải lên thành công
   * và các thành phần giao diện (UI components) đã được ánh xạ.
   * Tại đây, hệ thống sẽ tự động gửi một {@link GetActiveAuctionRequestDTO}
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

    // Gửi yêu cầu lấy danh sách ngay khi load UI
    ServerConnection.sendData(new GetActiveAuctionRequestDTO());
  }

  /**
   * Tải và hiển thị danh sách thẻ sản phẩm (Component) lên giao diện.
   * Hàm này sẽ được ResponseHandler gọi sau khi nhận được dữ liệu từ Server.
   * * @param auctions Danh sách các phiên đấu giá trả về từ Server.
   */
  public void loadFeedToUI(List<AuctionResponseDTO> auctions) {
    this.currentAuctions = auctions;
    // Bắt buộc dùng Platform.runLater để cập nhật UI an toàn từ luồng mạng (Network Thread)
    Platform.runLater(() -> {
      // Xóa các card cũ (nếu có) trước khi nạp mới
      feedContainer.getChildren().clear();

      for (AuctionResponseDTO auction : auctions) {
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
    for (AuctionResponseDTO auction : currentAuctions) {
      if (auction.getId().equals(auctionId)) {
        auction.setCurrentHighestPrice(newPrice);
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
  public void gotoProductDetail(AuctionResponseDTO selectedAuction) {
    // Lưu sản phẩm vừa chọn vào SessionManager
    SessionManager.setCurrentAuction(selectedAuction);
    System.out.println("Đang mở chi tiết phiên đấu giá: " + selectedAuction.getId());
    ScreenController.switchScreen("Bidder/ItemAuction.fxml", "Phiên đấu giá " + selectedAuction.getItem().getName());
  }

  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Xác nhận đăng xuất",
        "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        SessionManager.clearSession();
        ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");
      }
    });
  }

  @FXML
  public void gotoSellerHome() {
    ScreenController.navigateToSellerChannel();
  }

  @FXML
  public void gotoProfile() {
    ScreenController.switchScreen("User/Profile.fxml", "Thông tin tài khoản");
  }

  @FXML
  public void gotoWallet() {
    ScreenController.switchScreen("User/Wallet/Wallet.fxml", "Ví người dùng");
  }

  @FXML
  public void gotoResult() {
    ScreenController.switchScreen("Bidder/Result.fxml", "Kết quả đấu giá");
  }
}