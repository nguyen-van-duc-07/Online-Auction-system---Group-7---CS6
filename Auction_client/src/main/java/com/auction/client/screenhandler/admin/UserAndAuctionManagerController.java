package com.auction.client.screenhandler.admin;

import com.auction.client.screenhandler.ScreenController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class UserAndAuctionManagerController implements Initializable {

  @FXML
  private TextField searchField;

  @FXML
  private TableView<Object> dataTable;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // TODO: Tải dữ liệu ban đầu từ Server
  }

  /**
   * Lọc và tìm kiếm dữ liệu trên bảng theo từ khóa.
   */
  @FXML
  public void handleSearch() {
    String keyword = searchField.getText().trim();
    // TODO: Xử lý logic tìm kiếm
  }

  /**
   * Xử lý sự kiện thêm mới dữ liệu.
   */
  @FXML
  public void handleAdd() {
    // TODO: Hiển thị Dialog/Form thêm mới
  }

  /**
   * Xử lý sự kiện chỉnh sửa bản ghi đang được chọn.
   */
  @FXML
  public void handleEdit() {
    Object selected = dataTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn mục cần sửa.");
      return;
    }
    // TODO: Load Form chỉnh sửa cho selected
  }

  /**
   * Xử lý sự kiện xóa bản ghi đang được chọn.
   */
  @FXML
  public void handleDelete() {
    Object selected = dataTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn mục cần xóa.");
      return;
    }
    // TODO: Gửi request Xóa lên Server
    ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa thành công.");
  }
}
