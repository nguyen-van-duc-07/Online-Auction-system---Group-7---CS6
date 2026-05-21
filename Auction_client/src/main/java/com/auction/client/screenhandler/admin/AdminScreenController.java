package com.auction.client.screenhandler.admin;

import com.auction.client.network.SessionManager;
import com.auction.client.screenhandler.ScreenController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller quản lý màn hình chính của Admin.
 * Chịu trách nhiệm điều hướng và hiển thị các chức năng quản trị bên trong BorderPane.
 */
public class AdminScreenController implements Initializable {
  @FXML
  private VBox mainContent;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  /**
   * Mở màn hình quản lý Người dùng.
   */
  @FXML
  public void gotoUserManager() {
    loadComponent("/com/auction/client/Admin/UserManager.fxml");
  }

  @FXML
  public void gotoAuctionManager() {
    loadComponent("/com/auction/client/Admin/AuctionManager.fxml");
  }

  /**
   * Chuyển vùng nội dung bên trong VBox sang Quản lý duyệt Seller.
   */
  @FXML
  public void gotoSellerAccountManager() {
    loadComponent("/com/auction/client/Admin/SellerAccountManager.fxml");
  }

  /**
   * Đăng xuất khỏi hệ thống và chuyển về màn hình Login.
   */
  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION,
        "Đăng xuất", "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(Response -> {
          if  (Response == ButtonType.OK) {
            SessionManager.setCurrentUser(null);
            ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");
            ScreenController.primaryStage.setMaximized(false);
          }
    });
  }

  /**
   * Nạp file FXML và thay thế toàn bộ nội dung hiện tại của VBox.
   */
  private void loadComponent(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent newNode = loader.load();

      // Xóa sạch các node cũ và thêm node mới vào VBox
      mainContent.getChildren().setAll(newNode);

      // Đảm bảo nội dung mới luôn giãn nở hết cỡ theo VBox
      VBox.setVgrow(newNode, javafx.scene.layout.Priority.ALWAYS);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
