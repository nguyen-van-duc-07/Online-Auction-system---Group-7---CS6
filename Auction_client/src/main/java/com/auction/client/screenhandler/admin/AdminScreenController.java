package com.auction.client.screenhandler.admin;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.client.screenhandler.ScreenController;
import com.auction.shared.request.LogoutRequestDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller quản lý màn hình chính của Admin.
 * Chịu trách nhiệm điều hướng và hiển thị các chức năng quản trị bên trong BorderPane.
 */
public class AdminScreenController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(AdminScreenController.class);

  private static AdminScreenController instance;

  public static AdminScreenController getInstance() {
    return instance;
  }

  @FXML
  private VBox mainContent;

  @FXML
  private MenuButton adminMenuBtn;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;

    // Khởi tạo tên hiển thị của Admin chào mừng từ SessionManager
    if (SessionManager.getCurrentUser() != null) {
      String realName = SessionManager.getCurrentUser().getAccountName();
      String phoneNumber = SessionManager.getCurrentUser().getPhoneNumber();
      if (realName != null) {
        adminMenuBtn.setText("Xin chào: " + realName);
      } else if (phoneNumber != null) {
        adminMenuBtn.setText("Xin chào: " + phoneNumber);
      }
    }
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

  @FXML
  public void gotoPendingTransactionManager() {
    loadComponent("/com/auction/client/Admin/PendingTransactionManager.fxml");
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
            if (SessionManager.getCurrentUser() != null) {
              LogoutRequestDTO logoutRequestDTO = new LogoutRequestDTO();
              logoutRequestDTO.setUserId(SessionManager.getCurrentUser().getId());
              ServerConnection.sendData(logoutRequestDTO);
            }
            SessionManager.setCurrentUser(null);
            ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");
            ScreenController.primaryStage.setMaximized(false);
          }
    });
  }

  /**
   * Tải màn hình thông tin cá nhân của Admin.
   */
  @FXML
  public void gotoProfile() {
    loadComponent("/com/auction/client/User/Profile.fxml");
  }

  /**
   * Mở cửa sổ đổi mật khẩu.
   */
  @FXML
  public void gotoChangePassword() {
    ScreenController.createSubWindow("User/ChangePasswordForm.fxml", "Đổi mật khẩu");
  }

  /**
   * Cập nhật tên hiển thị của Admin trên MenuButton chào mừng.
   */
  public void updateAdminName(String newName) {
    if (adminMenuBtn != null) {
      adminMenuBtn.setText("Xin chào: " + newName);
    }
  }

  /**
   * Nạp file FXML và thay thế toàn bộ nội dung hiện tại của VBox.
   */
  public void loadComponent(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent newNode = loader.load();

      // Xóa sạch các node cũ và thêm node mới vào VBox
      mainContent.getChildren().setAll(newNode);

      // Đảm bảo nội dung mới luôn giãn nở hết cỡ theo VBox
      VBox.setVgrow(newNode, javafx.scene.layout.Priority.ALWAYS);

    } catch (IOException e) {
      log.error("Lỗi khi load Component FXML từ path: {}", fxmlPath, e);
    }
  }
}
