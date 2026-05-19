package com.auction.client.screenhandler.admin;

import com.auction.client.network.ServerConnection;
import com.auction.client.screenhandler.ScreenController;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.request.GetActiveAndWaitingAuctionsRequestDTO;
import com.auction.shared.request.SellerRegisterRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AuctionManagerController implements Initializable {
  /** Biến static lưu trữ Controller hiện tại của AuctionManagerController. */
  private static AuctionManagerController instance;

  @FXML
  private TextField searchField;

  @FXML
  private TableView<AuctionDTO> auctionTable;

  @FXML
  private TableColumn<AuctionDTO, Integer> serialColumn;

  @FXML
  private TableColumn<AuctionDTO, String> auctionIdColumn;

  @FXML
  private TableColumn<AuctionDTO, String> itemNameColumn;

  @FXML
  private TableColumn<AuctionDTO, LocalDateTime> startTimeColumn;

  @FXML
  private TableColumn<AuctionDTO, BigDecimal> currentHighestPriceColumn;

  @FXML
  private TableColumn<AuctionDTO, LocalDateTime> endTimeColumn;

  @FXML
  private TableColumn<AuctionDTO, String> statusColumn;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;
    setupTableColumns();
    ServerConnection.sendData(new GetActiveAndWaitingAuctionsRequestDTO());
  }

  /**
   * Cài đặt việc ánh xạ dữ liệu từ DTO vào các cột của bảng.
   */
  private void setupTableColumns() {
    // Cột STT (Tự động đánh số thứ tự)
    serialColumn.setCellFactory(col -> new TableCell<>() {
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
    startTimeColumn.setCellFactory(column -> new TableCell<>() {
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
    });
    endTimeColumn.setCellFactory(column -> new TableCell<>() {
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
    });

    serialColumn.setSortable(false); // Không cần sắp xếp cho cột STT

    serialColumn.setStyle("-fx-alignment: CENTER;"); // Căn giữa cho các cột
    auctionIdColumn.setStyle("-fx-alignment: CENTER;");
    itemNameColumn.setStyle("-fx-alignment: CENTER;");
    startTimeColumn.setStyle("-fx-alignment: CENTER;");
    currentHighestPriceColumn.setStyle("-fx-alignment: CENTER;");
    endTimeColumn.setStyle("-fx-alignment: CENTER;");
    statusColumn.setStyle("-fx-alignment: CENTER;");


    // Các cột còn lại ánh xạ đúng với tên biến trong AuctionRequestDTO.java
    auctionIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAuctionId()));
    itemNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemName()));
    startTimeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStartTime()));
    currentHighestPriceColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCurrentPrice()));
    endTimeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEndTime()));
    statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().toString()));
  }

  /**
   * Phương thức dùng để đẩy danh sách dữ liệu vào bảng.
   * @param auctionsList Danh sách các đơn đăng ký nhận từ Server
   */
  public void loadDataToTable(List<AuctionDTO> auctionsList) {
    if (auctionsList != null) {
      System.out.println("=== DỮ LIỆU NHẬN ĐƯỢC: " + auctionsList.size() + " đơn ==="); // THÊM DÒNG NÀY
      ObservableList<AuctionDTO> observableList = FXCollections.observableArrayList(auctionsList);
      auctionTable.setItems(observableList);
    } else {
      System.out.println("=== DỮ LIỆU NHẬN ĐƯỢC LÀ NULL ===");
    }
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
    Object selected = auctionTable.getSelectionModel().getSelectedItem();
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
    Object selected = auctionTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn mục cần xóa.");
      return;
    }
    // TODO: Gửi request Xóa lên Server
    ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa thành công.");
  }

  /**
   * Phương thức trả về controller hiện tại của màn hình.
   */
  public static AuctionManagerController getInstance() {
    return instance;
  }
}
