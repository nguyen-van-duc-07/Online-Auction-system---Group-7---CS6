package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.request.CheckingSellerProfileRequestDTO;
import com.auction.shared.request.GetAuctionsBySellerRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Class có nhiệm vụ quản lý màn hình seller.
 */
public class SellerHomeController implements Initializable, ProductDetailNavigator {
  /**
   * Biến static lưu trữ Controller hiện tại của SellerHome.
   */
  private static SellerHomeController instance;

  public static SellerHomeController getInstance() {
    return instance;
  }

  /** Khung chứa các thẻ sản phẩm, được ánh xạ từ fx:id="feedContainer" trong Seller/SellerHome.fxml. */
  @FXML
  private FlowPane feedContainer;

  HomeController homecontroller = new HomeController();


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Ghi nhận bản thân (this) làm instance hiện tại ngay khi màn hình vừa mở lên
    instance = this;

    // Gửi yêu cầu lấy danh sách ngay khi load UI
    String sellerId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetAuctionsBySellerRequestDTO(sellerId));
  }

  /**
   * Tải và hiển thị danh sách thẻ sản phẩm (Component) lên giao diện.
   * Hàm này sẽ được ResponseHandler gọi sau khi nhận được dữ liệu từ Server.
   * * @param auctions Danh sách các phiên đấu giá trả về từ Server.
   */
  public void loadFeedToUI(List<AuctionResponseDTO> auctions) {
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

          // Truyền object auction và 'this' (SellerHomeController) sang để thẻ con biết đường gọi chuyển trang
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
  public void gotoHomeWithHyperLink() {
    ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
  }

  @FXML
  public void gotoLogin() {
    homecontroller.gotoLogin();
  }

  @FXML
  public void gotoProfile() {
    homecontroller.gotoProfile();
  }

  @FXML
  public void gotoWallet() {
    homecontroller.gotoWallet();
  }

  @FXML
  public void gotoResult() {
    homecontroller.gotoResult();
  }

  @FXML
  public void gotoUploadItem() {
    ScreenController.switchScreen("Seller/UploadProduct.fxml", "Đăng sản phẩm");
  }
}