package com.auction.client.screenhandler.admin;

import com.auction.client.network.ServerConnection;
import com.auction.client.screenhandler.ScreenController;
import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.request.AuctionRequestDTO;
import com.auction.shared.request.GetActiveAndWaitingAuctionsRequestDTO;
import com.auction.shared.request.GetAuctionsRequestDTO;
import com.auction.shared.request.UpdateAuctionStatusRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller quản lý màn hình quản lý các phiên đấu giá dành cho Admin.
 * <p>
 * Lớp này cho phép Admin theo dõi danh sách các phiên đấu giá (đang diễn ra, chờ duyệt,
 * hoặc đã hủy), tìm kiếm, lọc theo trạng thái, mở, đóng hoặc chặn các phiên đấu giá.
 * </p>
 */
public class AuctionManagerController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(AuctionManagerController.class);

  /** Biến static lưu trữ Controller hiện tại của AuctionManagerController. */
  private static AuctionManagerController instance;

  private List<AuctionDTO> originalList;

  @FXML
  private TextField searchField;

  @FXML
  private TableView<AuctionDTO> auctionTable;

  @FXML
  private MenuButton typeMenuButton;

  private boolean showingCanceled = false;

  @FXML
  private Button openAuctionBtn;

  @FXML
  private Button closeAuctionBtn;

  @FXML
  private Button blockAuctionBtn;

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

    // Nhấp đúp chuột để xem chi tiết sản phẩm
    auctionTable.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        AuctionDTO selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
          AuctionRequestDTO req = new AuctionRequestDTO();
          req.setAuctionId(selected.getAuctionId());
          ServerConnection.sendData(req);
        }
      }
    });

    setShowingCanceled(false);
    ServerConnection.sendData(new GetActiveAndWaitingAuctionsRequestDTO());
  }

  private void updateButtonVisibility() {
    boolean showActionButtons = !showingCanceled;
    if (closeAuctionBtn != null) {
      closeAuctionBtn.setVisible(showActionButtons);
      closeAuctionBtn.setManaged(showActionButtons);
    }
    if (blockAuctionBtn != null) {
      blockAuctionBtn.setVisible(showActionButtons);
      blockAuctionBtn.setManaged(showActionButtons);
    }
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
    this.originalList = auctionsList;
    if (auctionsList != null) {
      log.info("=== DỮ LIỆU NHẬN ĐƯỢC: {} đơn ===", auctionsList.size());
      ObservableList<AuctionDTO> observableList = FXCollections.observableArrayList(auctionsList);
      auctionTable.setItems(observableList);
      handleSearch();
    } else {
      log.warn("=== DỮ LIỆU NHẬN ĐƯỢC LÀ NULL ===");
      auctionTable.setItems(FXCollections.observableArrayList());
    }
  }

  /**
   * Lọc và tìm kiếm dữ liệu trên bảng theo từ khóa.
   */
  @FXML
  public void handleSearch() {
    String keyword = searchField.getText().trim().toLowerCase();
    log.info("Tìm kiếm phiên đấu giá theo tên sản phẩm hoặc ID: {}", keyword);
    if (originalList == null) {
      return;
    }
    if (keyword.isEmpty()) {
      auctionTable.setItems(FXCollections.observableArrayList(originalList));
    } else {
      java.util.List<AuctionDTO> filtered = new java.util.ArrayList<>();
      for (AuctionDTO dto : originalList) {
        boolean matchesName = dto.getItemName() != null && dto.getItemName().toLowerCase().contains(keyword);
        boolean matchesId = dto.getAuctionId() != null && String.valueOf(dto.getAuctionId()).toLowerCase().contains(keyword);
        if (matchesName || matchesId) {
          filtered.add(dto);
        }
      }
      auctionTable.setItems(FXCollections.observableArrayList(filtered));
    }
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

  /**
   * Xử lý sự kiện khi Admin yêu cầu mở một phiên đấu giá.
   */
  @FXML
  public void handleOpenAuction() {
    AuctionDTO selected = auctionTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phiên đấu giá!");
      return;
    }

    if (selected.getStatus() == AuctionStatus.ACTIVE) {
      ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Phiên hiện đang mở sẵn!");
      return;
    }

    if (selected.getStatus() == AuctionStatus.WAITING || selected.getStatus() == AuctionStatus.CANCELED) {
      ServerConnection.sendData(new UpdateAuctionStatusRequestDTO(selected.getAuctionId(), AuctionStatus.ACTIVE));
    } else {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chỉ có thể mở phiên ở trạng thái chờ (WAITING) hoặc đã hủy (CANCELED)!");
    }
  }

  /**
   * Xử lý sự kiện khi Admin yêu cầu đóng một phiên đấu giá.
   */
  @FXML
  public void handleCloseAuction() {
    AuctionDTO selected = auctionTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phiên đấu giá!");
      return;
    }

    if (selected.getStatus() == AuctionStatus.ACTIVE || selected.getStatus() == AuctionStatus.WAITING) {
      ServerConnection.sendData(new UpdateAuctionStatusRequestDTO(selected.getAuctionId(), AuctionStatus.CLOSED));
    } else {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể đóng phiên đấu giá ở trạng thái này!");
    }
  }

  /**
   * Xử lý sự kiện khi Admin yêu cầu chặn một phiên đấu giá.
   */
  @FXML
  public void handleBlockAuction() {
    AuctionDTO selected = auctionTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một phiên đấu giá!");
      return;
    }

    if (selected.getStatus() == AuctionStatus.ACTIVE || selected.getStatus() == AuctionStatus.WAITING) {
      ServerConnection.sendData(new UpdateAuctionStatusRequestDTO(selected.getAuctionId(), AuctionStatus.CANCELED));
    } else {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể chặn phiên đấu giá ở trạng thái này!");
    }
  }

  /**
   * Tải lại danh sách các phiên đấu giá từ Server.
   */
  @FXML
  public void handleReload() {
    log.info("Yêu cầu tải lại danh sách phiên đấu giá (Đã huỷ: {}) từ server...", showingCanceled);
    if (showingCanceled) {
      ServerConnection.sendData(new GetAuctionsRequestDTO(AuctionStatus.CANCELED));
    } else {
      ServerConnection.sendData(new GetActiveAndWaitingAuctionsRequestDTO());
    }
  }

  /**
   * Cập nhật trạng thái hiển thị các phiên đấu giá đã hủy hoặc đang hoạt động.
   *
   * @param showingCanceled true nếu hiển thị các phiên đã hủy, false nếu hiển thị các phiên hoạt động
   */
  public void setShowingCanceled(boolean showingCanceled) {
    this.showingCanceled = showingCanceled;
    if (typeMenuButton != null) {
      typeMenuButton.setText(showingCanceled ? "Loại phiên: Đã huỷ" : "Loại phiên: Đang và sắp diễn ra");
    }
    updateButtonVisibility();
  }

  @FXML
  public void handleShowActiveAndWaiting() {
    setShowingCanceled(false);
    handleReload();
  }

  @FXML
  public void handleShowCanceled() {
    setShowingCanceled(true);
    handleReload();
  }
}
