package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.enums.ItemType;
import com.auction.shared.request.UploadItemRequestDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý logic cho màn hình Đăng bán sản phẩm.
 *
 * <p>Nhiệm vụ chính: Thu thập dữ liệu từ các form, xử lý gộp thời gian (Ngày + Giờ + Phút)
 * và ánh xạ danh mục sang Enum trước khi đóng gói gửi lên Server.</p>
 */
public class UploadItemController {
  private static final Logger log = LoggerFactory.getLogger(UploadItemController.class);
  /**
   * Trường nhập tên sản phẩm.
   */
  @FXML
  private TextField nameItemField;
  /**
   * Trường nhập giá khởi điểm.
   */
  @FXML
  private TextField iniPriceField;
  /**
   * Trường nhập bước giá tối thiểu.
   */
  @FXML
  private TextField minStepPriceField;
  /**
   * Trường nhập mô tả chi tiết sản phẩm.
   */
  @FXML
  private TextArea descriptionField;
  /**
   * Khung hiển thị hình ảnh sản phẩm.
   */
  @FXML
  private ImageView imageField;
  /**
   * Bộ chọn ngày bắt đầu.
   */
  @FXML
  private DatePicker startDateField;
  /**
   * ComboBox chọn giờ bắt đầu.
   */
  @FXML
  private ComboBox<String> startHourField;
  /**
   * ComboBox chọn phút bắt đầu.
   */
  @FXML
  private ComboBox<String> startMinuteField;

  /**
   * Bộ chọn ngày kết thúc.
   */
  @FXML
  private DatePicker finishDateField;
  /**
   * ComboBox chọn giờ kết thúc.
   */
  @FXML
  private ComboBox<String> finishHourField;
  /**
   * ComboBox chọn phút kết thúc.
   */
  @FXML
  private ComboBox<String> finishMinuteField;
  /**
   * ComboBox chọn danh mục sản phẩm.
   */
  @FXML
  private ComboBox<String> categoryField;

  @FXML
  private TextField durationField;

  /**
   * Khung chứa các trường nhập liệu động.
   */
  @FXML
  private VBox dynamicAttributesBox;

  private java.util.Map<String, TextField> dynamicTextFields = new java.util.HashMap<>();

  private File selectedImageFile;

  @FXML
  public void initialize() {
    for (int i = 0; i < 24; i++) {
        String h = String.format("%02d", i);
        startHourField.getItems().add(h);
        finishHourField.getItems().add(h);
    }
    for (int i = 0; i < 60; i++) {
        String m = String.format("%02d", i);
        startMinuteField.getItems().add(m);
        finishMinuteField.getItems().add(m);
    }

    categoryField.getItems().addAll(
        ItemType.ELECTRONICS.getValue(),
        ItemType.VEHICLES.getValue(),
        ItemType.COLLECTIBLES.getValue(),
        ItemType.FASHION.getValue(),
        ItemType.SPORTS.getValue(),
        ItemType.ARTS.getValue(),
        ItemType.OTHER.getValue()
    );

    categoryField.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      updateDynamicFields(newValue);
    });
  }

  private void updateDynamicFields(String category) {
    if (dynamicAttributesBox == null) return;
    dynamicAttributesBox.getChildren().clear();
    dynamicTextFields.clear();

    ItemType type = mapCategoryToEnum(category);
    List<String> attributes = new ArrayList<>();

    switch (type) {
      case ELECTRONICS -> attributes.addAll(java.util.Arrays.asList("Mẫu", "Thương hiệu", "Bảo hành", "Tình trạng"));
      case VEHICLES -> attributes.addAll(java.util.Arrays.asList("Thương hiệu", "Tình trạng", "Quãng đường đã đi", "Năm sản xuất", "Biển số xe", "Động cơ"));
      case COLLECTIBLES -> attributes.addAll(java.util.Arrays.asList("Độ hiếm", "Giấy chứng nhận", "Tình trạng"));
      case FASHION -> attributes.addAll(java.util.Arrays.asList("Thương hiệu", "Kiểu dáng", "Giới tính", "Kích cỡ", "Chất liệu"));
      case SPORTS -> attributes.addAll(java.util.Arrays.asList("Môn thể thao", "Thương hiệu", "Cân nặng/ Kích cỡ"));
      case ARTS -> attributes.addAll(java.util.Arrays.asList("Hoạ sĩ", "Giấy chứng nhận", "Năm xuất bản", "Kích cỡ"));
      default -> {}
    }

    for (String attr : attributes) {
      VBox vBox = new VBox(6.0);
      Label label = new Label(attr);
      TextField textField = new TextField();
      textField.setPromptText("Nhập " + attr);
      textField.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-background-color: #FFFFFF;");
      
      vBox.getChildren().addAll(label, textField);
      dynamicAttributesBox.getChildren().add(vBox);
      dynamicTextFields.put(attr, textField);
    }
  }

  /**
   * Xử lý sự kiện khi người dùng click vào nút "ĐĂNG SẢN PHẨM LÊN SÀN".
   *
   * <p>Kiểm tra quyền đăng nhập, parse dữ liệu thời gian và gửi RequestDTO qua Socket.</p>
   */
  @FXML
  public void uploadItem() {
    if (SessionManager.getCurrentUser() == null) {
      ScreenController.showAlert(Alert.AlertType.ERROR,
          "Lỗi truy cập", "Vui lòng đăng nhập để đăng sản phẩm!");
      return;
    }

    try {
      // Lấy thông tin cơ bản
      String sellerId = SessionManager.getCurrentUser().getId();
      String nameItem = nameItemField.getText().trim();
      String description = descriptionField.getText().trim();
      String startPriceStr = iniPriceField.getText().trim();
      String minStepPriceStr = minStepPriceField.getText().trim();

      if (sellerId.isEmpty() || nameItem.isEmpty() || description.isEmpty()
          || startPriceStr.isEmpty() || minStepPriceStr.isEmpty()) {
        ScreenController.showAlert(Alert.AlertType.WARNING,
            "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
        return;
      }

      BigDecimal startPrice = new BigDecimal(startPriceStr);
      BigDecimal minStepPrice = new BigDecimal(minStepPriceStr);

      // Xử lý Thời gian bắt đầu
      LocalDate startDate = startDateField.getValue();
      int startHour = startHourField.getValue() != null ? Integer.parseInt(startHourField.getValue()) : 0;
      int startMinute = startMinuteField.getValue() != null ? Integer.parseInt(startMinuteField.getValue()) : 0;

      // Nếu người dùng không chọn ngày bắt đầu, mặc định lấy thời gian hiện tại
      LocalDateTime startTime = (startDate != null)
          ? LocalDateTime.of(startDate, LocalTime.of(startHour, startMinute))
          : LocalDateTime.now();

      // Xử lý Thời lượng và Thời gian kết thúc
      String durationStr = durationField != null ? durationField.getText().trim() : "";
      LocalDateTime endTime;

      if (!durationStr.isEmpty()) {
          try {
              double durationHours = Double.parseDouble(durationStr);
              long minutesToAdd = (long) (durationHours * 60);
              endTime = startTime.plusMinutes(minutesToAdd);
          } catch (NumberFormatException e) {
              ScreenController.showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Thời lượng phải là một số (ví dụ: 24, 1.5)!");
              return;
          }
      } else {
          LocalDate finishDate = finishDateField.getValue();
          if (finishDate == null) {
            ScreenController.showAlert(Alert.AlertType.WARNING,
                "Cảnh báo", "Vui lòng chọn ngày kết thúc hoặc nhập thời lượng!");
            return;
          }
          int finishHour = finishHourField.getValue() != null ? Integer.parseInt(finishHourField.getValue()) : 23;
          int finishMinute = finishMinuteField.getValue() != null ? Integer.parseInt(finishMinuteField.getValue()) : 59;
          endTime = LocalDateTime.of(finishDate, LocalTime.of(finishHour, finishMinute));
      }

      // Kiểm tra logic thời gian cơ bản
      if (endTime.isBefore(startTime)) {
        ScreenController.showAlert(Alert.AlertType.WARNING,
            "Lỗi thời gian", "Thời gian kết thúc không thể diễn ra trước thời gian bắt đầu!");
        return;
      }

      // Xử lý Danh mục (Ánh xạ từ text giao diện sang Enum)
      ItemType type = mapCategoryToEnum(categoryField.getValue());

      // Đọc dữ liệu ảnh (nếu người dùng đã chọn ảnh)
      byte[] imageBytes = null;
      String imageExtension = null;
      if (selectedImageFile != null) {
        imageBytes = java.nio.file.Files.readAllBytes(selectedImageFile.toPath());
        String fileName = selectedImageFile.getName();
        imageExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
      }

      // Thu thập các thuộc tính động
      Map<String, String> additionalAttributes = new HashMap<>();
      for (Map.Entry<String, TextField> entry : dynamicTextFields.entrySet()) {
          additionalAttributes.put(entry.getKey(), entry.getValue().getText().trim());
      }

      UploadItemRequestDTO uploadItemRequestDTO = new UploadItemRequestDTO(sellerId,
                                                                          nameItem,
                                                                          type,
                                                                          description,
                                                                          startPrice,
                                                                          minStepPrice,
                                                                          startTime,
                                                                          endTime,
                                                                          imageBytes,
                                                                          imageExtension,
                                                                          additionalAttributes);

      ServerConnection.sendData(uploadItemRequestDTO);

    } catch (NumberFormatException e) {
      ScreenController.showAlert(Alert.AlertType.WARNING,
          "Lỗi nhập liệu", "Vui lòng nhập định dạng số hợp lệ cho các ô Giá tiền!");
    } catch (IllegalArgumentException e) {
      ScreenController.showAlert(Alert.AlertType.WARNING,
          "Cảnh báo", e.getMessage());
    } catch (Exception e) {
      ScreenController.showAlert(Alert.AlertType.ERROR,
          "Lỗi hệ thống", "Đã xảy ra lỗi không xác định!");
      log.error("Lỗi xảy ra trong quá trình đăng bán sản phẩm", e);
    }
  }

  /**
   * Hàm phụ trợ chuyển đổi chuỗi danh mục từ ComboBox sang kiểu {@link ItemType}.
   * * @param category Chuỗi danh mục lấy từ giao diện (ví dụ: "Thời trang")
   *
   * @return Biến Enum {@link ItemType} tương ứng
   */
  public ItemType mapCategoryToEnum(String category) {
    if (category == null) return ItemType.OTHER;

    return switch (category) {
      case "Thiết bị điện tử" -> ItemType.ELECTRONICS;
      case "Phương tiện di chuyển" -> ItemType.VEHICLES;
      case "Thời trang" -> ItemType.FASHION;
      case "Đồ sưu tầm" -> ItemType.COLLECTIBLES;
      case "Thể thao" -> ItemType.SPORTS;
      case "Nghệ thuật" -> ItemType.ARTS;
      default -> ItemType.OTHER;
    };
  }

  @FXML
  public void handleChooseImage() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Chọn ảnh sản phẩm đấu giá");
    FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
        "Image Files", "*.jpg", "*.png", "*.jpeg");
    fileChooser.getExtensionFilters().add(imageFilter);

    // Mở cửa sổ lên và "đứng chờ" người dùng chọn file
    File file = fileChooser.showOpenDialog(ScreenController.primaryStage);

    if (file != null) {
      // Kiểm tra dung lượng file <= 5MB (5 * 1024 * 1024 bytes)
      if (file.length() > 5 * 1024 * 1024) {
        ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo",
            "Kích thước ảnh quá lớn. Vui lòng chọn ảnh có dung lượng từ 5MB trở xuống!");
        return;
      }

      // Lưu trữ ảnh vừa chọn vào trong selectedImageFile để tí nữa upload lên khi người dùng ấn upload
      selectedImageFile = file;

      // Ép đường dẫn file thành dạng chuỗi URI để JavaFX hiểu
      Image image = new Image(file.toURI().toString());

      // Hiển thị ảnh lên phần khung trên màn hình
      imageField.setImage(image);
    }
  }
}
