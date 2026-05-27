package com.auction.client.screenhandler.admin;

import com.auction.client.network.ServerConnection;
import com.auction.client.screenhandler.HomeController;
import com.auction.client.screenhandler.ScreenController;
import com.auction.shared.enums.SellerRegisterStatus;
import com.auction.shared.model.item.Item;
import com.auction.shared.request.CancelSellerAuctionsRequestDTO;
import com.auction.shared.request.GetSellerProfileRequestDTO;
import com.auction.shared.request.RestoreSellerAuctionsRequestDTO;
import com.auction.shared.request.SellerRegisterRequestDTO;
import com.auction.shared.request.UpdateSellerProfileStatusRequestDTO;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller xử lý nghiệp vụ xét duyệt yêu cầu trở thành Seller của Bidder.
 */
public class SellerAccountManagerController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(SellerAccountManagerController.class);

  /** Biến static lưu trữ Controller hiện tại của SellerAccountManagerController. */
  private static SellerAccountManagerController instance;

  private List<SellerRegisterRequestDTO> originalList;

  @FXML
  private TextField searchField;

  @FXML
  private TableView<SellerRegisterRequestDTO> sellerProfileTable;

  @FXML
  private TableColumn<SellerRegisterRequestDTO, Integer> serialColumn;

  @FXML
  private TableColumn<SellerRegisterRequestDTO, LocalDateTime> createAtColumn;

  @FXML
  private TableColumn<SellerRegisterRequestDTO, String> userIdColumn;

  @FXML
  private TableColumn<SellerRegisterRequestDTO, String> brandNameColumn;

  @FXML
  private TableColumn<SellerRegisterRequestDTO, String> locationColumn;

  @FXML
  private TableColumn<SellerRegisterRequestDTO, String> bankAccountColumn;

  @FXML
  private TableColumn<SellerRegisterRequestDTO, String> bankNameColumn;

  @FXML
  private TableColumn<SellerRegisterRequestDTO, String> citizenIdentityCardColumn;

  @FXML
  private TableColumn<SellerRegisterRequestDTO, String> statusColumn;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;
    setupTableColumns();
    ServerConnection.sendData(new GetSellerProfileRequestDTO());
  }

  /**
   * Tìm kiếm đơn đăng ký.
   */
  @FXML
  public void handleSearch() {
    String keyword = searchField.getText().trim().toLowerCase();
    log.info("Tìm kiếm đơn đăng kí theo tên cửa hàng: {}", keyword);
    if (originalList == null) {
      return;
    }
    if (keyword.isEmpty()) {
      sellerProfileTable.setItems(FXCollections.observableArrayList(originalList));
    } else {
      java.util.List<SellerRegisterRequestDTO> filtered = new java.util.ArrayList<>();
      for (SellerRegisterRequestDTO dto : originalList) {
        if (dto.getBrandName() != null && dto.getBrandName().toLowerCase().contains(keyword)) {
          filtered.add(dto);
        }
      }
      sellerProfileTable.setItems(FXCollections.observableArrayList(filtered));
    }
  }

  /**
   * Cài đặt việc ánh xạ dữ liệu từ DTO vào các cột của bảng.
   */
  private void setupTableColumns() {
    // Cột STT (Tự động đánh số thứ tự)
    serialColumn.setCellFactory(col -> new javafx.scene.control.TableCell<SellerRegisterRequestDTO, Integer>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setText(null);
        } else {
          setText(String.valueOf(getIndex() + 1));
        }
      }
    });

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    createAtColumn.setCellFactory(column -> {
      return new TableCell<SellerRegisterRequestDTO, LocalDateTime>() {
        @Override
        protected void updateItem(LocalDateTime item, boolean empty) {
          super.updateItem(item, empty);
          if (item == null || empty) {
            setText(null);
          } else {
            // Chuyển LocalDateTime sang String theo format đã chọn
            setText(formatter.format(item));
          }
        }
      };
    });

    serialColumn.setSortable(false); // Không cần sắp xếp cho cột STT

    serialColumn.setStyle("-fx-alignment: CENTER;"); // Căn giữa cho các cột
    createAtColumn.setStyle("-fx-alignment: CENTER;");
    userIdColumn.setStyle("-fx-alignment: CENTER;");
    brandNameColumn.setStyle("-fx-alignment: CENTER;");
    locationColumn.setStyle("-fx-alignment: CENTER;");
    bankAccountColumn.setStyle("-fx-alignment: CENTER;");
    bankNameColumn.setStyle("-fx-alignment: CENTER;");
    citizenIdentityCardColumn.setStyle("-fx-alignment: CENTER;");
    statusColumn.setStyle("-fx-alignment: CENTER;");


    // Các cột còn lại ánh xạ đúng với tên biến trong SellerRegisterRequestDTO.java
    createAtColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCreatedAt()));
    userIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserId()));
    brandNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrandName()));
    locationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));
    bankAccountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBankAccount()));
    bankNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBankName()));
    citizenIdentityCardColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCitizenIdentityCard()));
    statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
  }

  /**
   * Phương thức dùng để đẩy danh sách dữ liệu vào bảng.
   * @param requestList Danh sách các đơn đăng ký nhận từ Server
   */
  public void loadDataToTable(List<SellerRegisterRequestDTO> requestList) {
    this.originalList = requestList;
    if (requestList != null) {
      log.info("=== DỮ LIỆU NHẬN ĐƯỢC: {} đơn ===", requestList.size());
      ObservableList<SellerRegisterRequestDTO> observableList = FXCollections.observableArrayList(requestList);
      sellerProfileTable.setItems(observableList);
      handleSearch();
    } else {
      log.warn("=== DỮ LIỆU NHẬN ĐƯỢC LÀ NULL ===");
      sellerProfileTable.setItems(FXCollections.observableArrayList());
    }
  }

  /**
   * Chấp nhận yêu cầu cấp quyền Seller đang được chọn trong bảng.
   */
  @FXML
  public void handleAccept() {
    SellerRegisterRequestDTO selected = sellerProfileTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING,
          "Cảnh báo", "Vui lòng chọn một đơn để duyệt!");
      return;
    }
    if (selected.getStatus().equals(SellerRegisterStatus.REGISTERED.toString())) {
      ScreenController.showAlert(Alert.AlertType.WARNING,
          "Cảnh báo", "Hồ sơ đã được duyệt trước đó!");
      return;
    }
    UpdateSellerProfileStatusRequestDTO request = new UpdateSellerProfileStatusRequestDTO();
    request.setUserId(selected.getUserId());
    request.setNewStatus(SellerRegisterStatus.REGISTERED);
    request.setExpectedOldStatus(SellerRegisterStatus.valueOf(selected.getStatus()));
    ServerConnection.sendData(request);
  }

  /**
   * Từ chối yêu cầu cấp quyền Seller đang được chọn trong bảng.
   */
  @FXML
  public void handleReject() {
    SellerRegisterRequestDTO selected = sellerProfileTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING,
          "Cảnh báo", "Vui lòng chọn một đơn để từ chối!");
      return;
    }
    if (selected.getStatus().equals(SellerRegisterStatus.DENIED.toString())) {
      ScreenController.showAlert(Alert.AlertType.WARNING,
          "Cảnh báo", "Hồ sơ đã bị từ chối trước đó!");
      return;
    }
    UpdateSellerProfileStatusRequestDTO request = new UpdateSellerProfileStatusRequestDTO();
    request.setUserId(selected.getUserId());
    request.setNewStatus(SellerRegisterStatus.DENIED);
    request.setExpectedOldStatus(SellerRegisterStatus.valueOf(selected.getStatus()));
    ServerConnection.sendData(request);
  }

  /**
   * Phương thức trả về controller hiện tại của màn hình.
   */
  public static SellerAccountManagerController getInstance() {
    return instance;
  }

  @FXML
  public void handleReload() {
    log.info("Yêu cầu tải lại danh sách hồ sơ đăng ký Seller từ server...");
    ServerConnection.sendData(new GetSellerProfileRequestDTO());
  }
}
