package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.request.GetActiveAuctionRequestDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller xử lý logic cho màn hình trang chủ.
 * Chịu trách nhiệm hiển thị danh sách sản phâ đấu giá động từ Server.
 */
public class HomeController implements Initializable {

  /** Biến static lưu trữ Controller hiện tại của Home. */
  private static HomeController instance;


  public static HomeController getInstance() {
    return instance;
  }

  /** Khung chứa các thẻ sản phẩm, được ánh xạ từ fx:id="feedContainer" trong Home.fxml. */
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
   * Vẽ và hiển thị danh sách sản phẩm lên giao diện.
   * Hàm này sẽ được ResponseHandler gọi sau khi nhận được dữ liệu từ Server.
   * * @param auctions Danh sách các phiên đấu giá trả về từ Server.
   */
  public void loadFeedToUI(List<AuctionResponseDTO> auctions) {
    // Xóa các card cũ (nếu có) trước khi nạp mới
    feedContainer.getChildren().clear();

    // Định dạng thời gian hiển thị
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    for (AuctionResponseDTO auction : auctions) {
      // Tạo khung thẻ sản phẩm (Card)
      VBox card = new VBox(10);
      card.setPadding(new Insets(15));
      card.setPrefWidth(180); // Khớp với kích thước cũ trong FXML của nhóm bạn
      card.setAlignment(Pos.CENTER);
      card.setStyle("-fx-background-color: #ffffff; "
          + "-fx-border-color: #e6e6e6; "
          + "-fx-border-radius: 8; "
          + "-fx-background-radius: 8; "
          + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

      // Ảnh sản phẩm
      ImageView imageView = new ImageView();
      imageView.setFitHeight(100.0);
      imageView.setFitWidth(120.0);
      imageView.setPreserveRatio(true);

      // Tên sản phẩm
      Label nameLabel = new Label(auction.getItem().getName());
      nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
      nameLabel.setTextFill(javafx.scene.paint.Color.web("#333333"));

      // Format và in Giá hiện tại
      // Dấu %,.0f sẽ tự động thêm dấu phẩy ngăn cách hàng nghìn và bỏ phần thập phân
      String formattedPrice = String.format("%,.0f VNĐ", auction.getCurrentHighestPrice());
      Label priceLabel = new Label("Giá: " + formattedPrice);
      priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
      priceLabel.setTextFill(javafx.scene.paint.Color.web("#e74c3c"));

      // Thời gian kết thúc
      Label timeLabel = new Label("Kết thúc: " + auction.getEndTime().format(formatter));
      timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d;");

      // Nút Đấu giá
      Button bidButton = new Button("Đấu giá");
      bidButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; "
          + "-fx-background-radius: 4; -fx-padding: 6 12 6 12; -fx-font-weight: bold;");
      bidButton.setOnAction(e -> gotoProductDetail(auction));

      // Thêm tất cả vào card
      card.getChildren().addAll(imageView, nameLabel, priceLabel, timeLabel, bidButton);

      // Đưa card vào FlowPane chính
      feedContainer.getChildren().add(card);
    }
  }

  /**
   * Chuyển hướng sang màn hình chi tiết sản phẩm.
   */
  private void gotoProductDetail(AuctionResponseDTO selectedAuction) {
    // Lưu sản phẩm vừa chọn vào SessionManager
    SessionManager.setCurrentAuction(selectedAuction);
    System.out.println("Đang mở chi tiết phiên đấu giá: " + selectedAuction.getId());
    ScreenController.switchScreen("ItemAuction.fxml", "Phiên đấu giá " + selectedAuction.getItem().getName());
  }

  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Xác nhận đăng xuất",
        "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        ScreenController.switchScreen("Login.fxml", "Đăng nhập");
      }
    });
  }

  @FXML
  public void gotoSellerHome() {
    ScreenController.switchScreen("SellerHome.fxml", "Quản lý sản phẩm");
  }

  @FXML
  public void gotoProfile() {
    ScreenController.switchScreen("Profile.fxml", "Thông tin tài khoản");
  }

  @FXML
  public void gotoWallet() {
    ScreenController.switchScreen("Wallet.fxml", "Ví người dùng");
  }

  @FXML
  public void gotoResult() {
    ScreenController.switchScreen("Result.fxml", "Kết quả đấu giá");
  }
}