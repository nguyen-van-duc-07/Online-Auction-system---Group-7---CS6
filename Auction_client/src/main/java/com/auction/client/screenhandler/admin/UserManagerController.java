package com.auction.client.screenhandler.admin;

import com.auction.client.network.ServerConnection;
import com.auction.client.screenhandler.ScreenController;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.GetAllUsersRequestDTO;
import com.auction.shared.request.CreateAdminRequestDTO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UserManagerController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(UserManagerController.class);

  private static UserManagerController instance;

  public static UserManagerController getInstance() {
    return instance;
  }

  private List<UserDTO> originalList;

  @FXML
  private TextField searchField;

  @FXML
  private TableView<UserDTO> userTable;

  @FXML
  private VBox createAdminForm;

  @FXML
  private TextField adminPhoneField;

  @FXML
  private PasswordField adminPasswordField;

  @FXML
  private TableColumn<UserDTO, Integer> serialColumn;

  @FXML
  private TableColumn<UserDTO, String> realNameColumn;

  @FXML
  private TableColumn<UserDTO, LocalDate> dobColumn;

  @FXML
  private TableColumn<UserDTO, String> phoneNumberColumn;

  @FXML
  private TableColumn<UserDTO, String> emailColumn;

  @FXML
  private TableColumn<UserDTO, String> addressColumn;

  @FXML
  private TableColumn<UserDTO, String> roleColumn;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;
    setupTableColumns();
    ServerConnection.sendData(new GetAllUsersRequestDTO());
  }

  /**
   * Cài đặt ánh xạ các cột hiển thị trên TableView.
   */
  private void setupTableColumns() {
    // Cột STT tự động tăng
    serialColumn.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
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
    serialColumn.setSortable(false);

    // Định dạng ngày sinh dd/MM/yyyy
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    dobColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
      @Override
      protected void updateItem(LocalDate item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
          setText(null);
        } else {
          setText(formatter.format(item));
        }
      }
    });

    // Căn giữa các cột thông tin chính
    serialColumn.setStyle("-fx-alignment: CENTER;");
    dobColumn.setStyle("-fx-alignment: CENTER;");
    phoneNumberColumn.setStyle("-fx-alignment: CENTER;");
    roleColumn.setStyle("-fx-alignment: CENTER;");

    // Ánh xạ các trường dữ liệu từ UserDTO
    realNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccountName()));
    dobColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDob()));
    phoneNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhoneNumber()));
    emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
    addressColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));
    roleColumn.setCellValueFactory(data -> new SimpleStringProperty(
        data.getValue().getRole() != null ? data.getValue().getRole().toString() : ""
    ));
  }

  /**
   * Đẩy danh sách người dùng vào TableView.
   */
  public void loadDataToTable(List<UserDTO> usersList) {
    this.originalList = usersList;
    if (usersList != null) {
      log.info("=== TẢI DANH SÁCH NGƯỜI DÙNG: {} tài khoản ===", usersList.size());
      ObservableList<UserDTO> observableList = FXCollections.observableArrayList(usersList);
      userTable.setItems(observableList);
      handleSearch();
    } else {
      log.warn("=== DANH SÁCH NGƯỜI DÙNG LÀ NULL ===");
      userTable.setItems(FXCollections.observableArrayList());
    }
  }

  /**
   * Tìm kiếm người dùng động theo tên, số điện thoại hoặc email.
   */
  @FXML
  public void handleSearch() {
    String keyword = searchField.getText().trim().toLowerCase();
    log.info("Tìm kiếm người dùng theo từ khóa: {}", keyword);
    if (originalList == null) {
      return;
    }
    if (keyword.isEmpty()) {
      userTable.setItems(FXCollections.observableArrayList(originalList));
    } else {
      List<UserDTO> filtered = new ArrayList<>();
      for (UserDTO dto : originalList) {
        boolean matchesRealName = dto.getAccountName() != null && dto.getAccountName().toLowerCase().contains(keyword);
        boolean matchesPhone = dto.getPhoneNumber() != null && dto.getPhoneNumber().toLowerCase().contains(keyword);
        boolean matchesEmail = dto.getEmail() != null && dto.getEmail().toLowerCase().contains(keyword);
        if (matchesRealName || matchesPhone || matchesEmail) {
          filtered.add(dto);
        }
      }
      userTable.setItems(FXCollections.observableArrayList(filtered));
    }
  }

  @FXML
  public void handleAdd() {
    // TODO: Hiển thị Dialog/Form thêm mới người dùng
  }

  @FXML
  public void handleEdit() {
    UserDTO selected = userTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn người dùng cần sửa.");
      return;
    }
    // TODO: Form chỉnh sửa người dùng
  }

  @FXML
  public void handleDelete() {
    UserDTO selected = userTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn người dùng cần xóa.");
      return;
    }
    // TODO: Gửi yêu cầu xóa người dùng lên Server
    ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa thành công.");
  }

  @FXML
  public void handleShowCreateAdminForm() {
    createAdminForm.setVisible(true);
    createAdminForm.setManaged(true);
    adminPhoneField.clear();
    adminPasswordField.clear();
  }

  @FXML
  public void handleCancelCreateAdmin() {
    createAdminForm.setVisible(false);
    createAdminForm.setManaged(false);
    adminPhoneField.clear();
    adminPasswordField.clear();
  }

  public void hideCreateAdminSection() {
    createAdminForm.setVisible(false);
    createAdminForm.setManaged(false);
    adminPhoneField.clear();
    adminPasswordField.clear();
  }

  @FXML
  public void handleCreateAdminSubmit() {
    String phoneNumber = adminPhoneField.getText().trim();
    String password = adminPasswordField.getText().trim();

    if (phoneNumber.isEmpty() || password.isEmpty()) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ số điện thoại và mật khẩu!");
      return;
    }

    CreateAdminRequestDTO request = new CreateAdminRequestDTO();
    request.setPhoneNumber(phoneNumber);
    request.setPassword(password);
    request.setAccountName("");

    ServerConnection.sendData(request);
  }

  @FXML
  public void handleReload() {
    log.info("Yêu cầu tải lại danh sách người dùng từ server...");
    ServerConnection.sendData(new GetAllUsersRequestDTO());
  }
}
