package com.auction.client.screenhandler.admin;

import com.auction.client.network.ServerConnection;
import com.auction.client.screenhandler.HomeController;
import com.auction.client.screenhandler.ScreenController;
import com.auction.shared.enums.SellerRegisterStatus;
import com.auction.shared.model.item.Item;
import com.auction.shared.request.GetSellerProfileRequestDTO;
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

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller xử lý nghiệp vụ xét duyệt yêu cầu trở thành Seller của Bidder.
 */
public class SellerAccountManagerController implements Initializable {
  /** Biến static lưu trữ Controller hiện tại của SellerAccountManagerController. */
  private static SellerAccountManagerController instance;

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
    String keyword = searchField.getText().trim();
    // TODO: Lọc danh sách theo keyword
    System.out.println("Tìm kiếm đơn đăng kí: " + keyword);
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
    if (requestList != null) {
      System.out.println("=== DỮ LIỆU NHẬN ĐƯỢC: " + requestList.size() + " đơn ==="); // THÊM DÒNG NÀY
      ObservableList<SellerRegisterRequestDTO> observableList = FXCollections.observableArrayList(requestList);
      sellerProfileTable.setItems(observableList);
    } else {
      System.out.println("=== DỮ LIỆU NHẬN ĐƯỢC LÀ NULL ===");
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
          "Cảnh báo", "Vui lòng chọn một đơn để duyệt.");
      return;
    }
    UpdateSellerProfileStatusRequestDTO request = new UpdateSellerProfileStatusRequestDTO();
    request.setUserId(selected.getUserId());
    request.setNewStatus(SellerRegisterStatus.REGISTERED);
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
          "Cảnh báo", "Vui lòng chọn một đơn để từ chối.");
      return;
    }
    UpdateSellerProfileStatusRequestDTO request = new UpdateSellerProfileStatusRequestDTO();
    request.setUserId(selected.getUserId());
    request.setNewStatus(SellerRegisterStatus.DENIED);
    ServerConnection.sendData(request);
  }

  /**
   * Phương thức trả về controller hiện tại của màn hình.
   */
  public static SellerAccountManagerController getInstance() {
    return instance;
  }
}
