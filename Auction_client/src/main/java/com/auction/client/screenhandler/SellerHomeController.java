package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.AuctionDTO;
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

import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

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

  HomeController homecontroller = HomeController.getInstance();


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
  public void loadFeedToUI(List<AuctionDTO> auctions) {
    // Bắt buộc dùng Platform.runLater để cập nhật UI an toàn từ luồng mạng (Network Thread)
    Platform.runLater(() -> {
      // Xóa các card cũ (nếu có) trước khi nạp mới
      feedContainer.getChildren().clear();

      VBox addNewCard = new VBox(5); // Khoảng cách giữa các phần tử là 10px

      // CHÚ Ý: Đổi 220, 300 thành kích thước đúng với AuctionItemCard.fxml của bạn
      addNewCard.setPrefSize(760, 110);
      addNewCard.setAlignment(Pos.CENTER);

      // Trang trí CSS cho Card (Nền xám nhạt, viền bo góc)
      // Khi bình thường
      String normalStyle = "-fx-background-color: #27ae60; -fx-border-color: #219653; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;";
      // Khi di chuột vào (Sáng hơn)
      String hoverStyle = "-fx-background-color: #2ecc71; -fx-border-color: #27ae60; -fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;";
      addNewCard.setStyle(normalStyle);

      // Hiệu ứng Hover (Làm sáng lên khi đưa chuột vào)
      addNewCard.setOnMouseEntered(e -> addNewCard.setStyle(hoverStyle));
      addNewCard.setOnMouseExited(e -> addNewCard.setStyle(normalStyle));

      // Tạo Label dấu + và Text
      Label plusIcon = new Label("+");
      plusIcon.setStyle("-fx-font-size: 60px; -fx-text-fill: white; -fx-font-weight: bold;");

      Label textLabel = new Label("Đăng bán sản phẩm");
      textLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

      addNewCard.getChildren().addAll(plusIcon, textLabel);

      // Bắt sự kiện Click để chuyển sang trang Upload
      addNewCard.setOnMouseClicked(event -> gotoUploadItem());

      // Thêm card này vào Container đầu tiên
      feedContainer.getChildren().add(addNewCard);

      for (AuctionDTO auction : auctions) {
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
  public void gotoProductDetail(AuctionDTO selectedAuction) {
    // Lưu sản phẩm vừa chọn vào SessionManager
    SessionManager.setCurrentAuctionId(selectedAuction.getAuctionId());
    System.out.println("Đang mở chi tiết phiên đấu giá: " + selectedAuction.getAuctionId());
    ScreenController.switchScreen("Bidder/ItemAuction.fxml", "Phiên đấu giá " + selectedAuction.getItemName());
  }

  public void gotoUploadItem() {
    homecontroller.loadComponent("/com/auction/client/Seller/UploadItem.fxml");
  }
}