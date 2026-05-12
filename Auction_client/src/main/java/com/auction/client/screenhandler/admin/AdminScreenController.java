package com.auction.client.screenhandler.admin;

import com.auction.client.network.SessionManager;
import com.auction.client.screenhandler.ScreenController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller quản lý màn hình chính của Admin.
 * Chịu trách nhiệm điều hướng và hiển thị các chức năng quản trị bên trong BorderPane.
 */
public class AdminScreenController implements Initializable {
  @FXML
  private TextField searchField;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  /**
   * Mở màn hình quản lý Người dùng.
   */
  @FXML
  public void gotoUserManager() {
    ScreenController.createSubWindow(
        "Admin/UserAndAuctionManager.fxml", "Quản lý Người dùng");
  }

  @FXML
  public void gotoAuctionManager() {
    ScreenController.createSubWindow(
        "Admin/UserAndAuctionManager.fxml", "Quản lý Phiên đấu giá");
  }

  /**
   * Mở màn hình duyệt yêu cầu Seller.
   */
  @FXML
  public void gotoSellerAccountManager() {
    ScreenController.createSubWindow(
        "Admin/SellerAccountManager.fxml", "QUản lý yêu cầu đăng kí Người bán");
  }

  /**
   * Đăng xuất khỏi hệ thống và chuyển về màn hình Login.
   */
  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION,
        "Đăng xuất", "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(Response -> {
          if  (Response == ButtonType.OK) {
            SessionManager.clearSession();
            ScreenController.switchScreen("Login.fxml", "Đăng nhập");
          }
    });
  }

  /**
   * Tìm kiếm chung toàn hệ thống.
   */
  public void handleSearch() {
    String keyword = searchField.getText();
  }
}
