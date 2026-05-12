package com.auction.client.screenhandler.admin;

import com.auction.client.screenhandler.ScreenController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller xử lý nghiệp vụ xét duyệt yêu cầu trở thành Seller của Bidder.
 */
public class SellerAccountManagerController implements Initializable {
  @FXML
  private TextField searchField;

  @FXML
  private TableView<Object> pendingRequestsTable;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // TODO: Gửi request lấy danh sách duyệt và bind vào TableView
  }

  /**
   * Tìm kiếm đơn đăng ký.
   */
  @FXML
  public void handleSearch() {
    String keyword = searchField.getText().trim();
    // TODO: Lọc danh sách theo keyword
    System.out.println("Tìm kiếm đơn đăng kí: " + keyword);
  }

  /**
   * Chấp nhận yêu cầu cấp quyền Seller đang được chọn trong bảng.
   */
  @FXML
  public void handleAccept() {
    Object selected = pendingRequestsTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING,
          "Cảnh báo", "Vui lòng chọn một đơn để duyệt.");
      return;
    }
    // TODO: Gửi request chấp nhận lên Server
    ScreenController.showAlert(Alert.AlertType.INFORMATION,
        "Thành công", "Đã duyệt yêu cầu nâng cấp Seller.");
  }

  /**
   * Từ chối yêu cầu cấp quyền Seller đang được chọn trong bảng.
   */
  @FXML
  public void handleReject() {
    Object selected = pendingRequestsTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING,
          "Cảnh báo", "Vui lòng chọn một đơn để từ chối.");
      return;
    }
    // TODO: Gửi request từ chối lên Server
    ScreenController.showAlert(Alert.AlertType.INFORMATION,
        "Thành công", "Đã từ chối yêu cầu nâng cấp Seller.");
  }
}
