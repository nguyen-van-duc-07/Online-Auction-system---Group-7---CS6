package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.enums.ItemType;
import com.auction.shared.request.UploadItemRequestDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Controller xử lý logic cho màn hình Đăng bán sản phẩm.
 *
 * <p>Nhiệm vụ chính: Thu thập dữ liệu từ các form, xử lý gộp thời gian (Ngày + Giờ + Phút)
 * và ánh xạ danh mục sang Enum trước khi đóng gói gửi lên Server.</p>
 */
public class UploadItemController {
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

  private File selectedImageFile;

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

      // Xử lý Thời gian kết thúc
      LocalDate finishDate = finishDateField.getValue();
      if (finishDate == null) {
        ScreenController.showAlert(Alert.AlertType.WARNING,
            "Cảnh báo", "Vui lòng chọn ngày kết thúc!");
        return;
      }
      int finishHour = finishHourField.getValue() != null ? Integer.parseInt(finishHourField.getValue()) : 23;
      int finishMinute = finishMinuteField.getValue() != null ? Integer.parseInt(finishMinuteField.getValue()) : 59;
      LocalDateTime endTime = LocalDateTime.of(finishDate, LocalTime.of(finishHour, finishMinute));

      // Kiểm tra logic thời gian cơ bản
      if (endTime.isBefore(startTime)) {
        ScreenController.showAlert(Alert.AlertType.WARNING,
            "Lỗi thời gian", "Thời gian kết thúc không thể diễn ra trước thời gian bắt đầu!");
        return;
      }

      // Xử lý Danh mục (Ánh xạ từ text giao diện sang Enum)
      ItemType type = mapCategoryToEnum(categoryField.getValue());

      UploadItemRequestDTO uploadItemRequestDTO = new UploadItemRequestDTO(sellerId,
                                                                          nameItem,
                                                                          type,
                                                                          description,
                                                                          startPrice,
                                                                          minStepPrice,
                                                                          startTime,
                                                                          endTime);

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
      e.printStackTrace();
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
      // Lưu trữ ảnh vừa chọn vào trong selectedImageFile để tí nữa upload lên khi người dùng ấn upload
      selectedImageFile = file;

      // Ép đường dẫn file thành dạng chuỗi URI để JavaFX hiểu
      Image image = new Image(file.toURI().toString());

      // Hiển thị ảnh lên phần khung trên màn hình
      imageField.setImage(image);
    }
  }
}
