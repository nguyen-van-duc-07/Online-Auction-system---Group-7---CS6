package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.UpdateProfileRequestDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller xử lý logic cho màn hình cập nhật thông tin tài khoản (Profile).
 *
 * <p>Triển khai {@link Initializable} để tự động nạp dữ liệu người dùng từ Session khi màn hình khởi tạo.</p>
 */
public class ProfileController implements Initializable {
  private String realName;
  private String email;
  private String phoneNumber;
  private String address;
  private LocalDate birthDate;
  private File selectedImageFile;

  HomeController homeController = new HomeController();

  @FXML
  private TextField realNameField;
  @FXML
  private TextField emailField;
  @FXML
  private DatePicker dateOfBirthField;
  @FXML
  private TextField phoneNumberField;
  @FXML
  private TextField addressField;
  @FXML
  private ImageView imageViewField;

  /**
   * Hàm được tự động gọi khi màn hình Profile.fxml được load lên.
   *
   * <p>Truy xuất dữ liệu từ {@link SessionManager} và điền tự động vào các TextField.</p>
   */
  public void initialize(URL location, ResourceBundle resources) {
    UserDTO currentUser = SessionManager.getCurrentUser();

    if (currentUser != null) {
      if (currentUser.getAccountName() != null) {
        if (currentUser.getRealName() != null) {
          realNameField.setText(currentUser.getRealName());
        }
        if (currentUser.getEmail() != null) {
          emailField.setText(currentUser.getEmail());
        }
        if (currentUser.getPhoneNumber() != null) {
          phoneNumberField.setText(currentUser.getPhoneNumber());
        }
        if (currentUser.getDob() != null) {
          dateOfBirthField.setValue(currentUser.getDob());
        }
        if (currentUser.getAddress() != null) {
          addressField.setText(currentUser.getAddress());
        }
      }
    }
  }

  /**
   * Xử lý sự kiện đăng xuất của người dùng.
   *
   * <p>Xác nhận với người dùng, xóa {@link SessionManager} và điều hướng về trang Login.</p>
   */
  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Bạn chưa lưu thay đổi",
        "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        ScreenController.switchScreen("Login.fxml", "Đăng nhập");
        SessionManager.clearSession(); // Xoá người dùng hiện tại ra khỏi SessionManager
      }
    });
  }

  public boolean isInformationValid() {
    realName = realNameField.getText();
    email = emailField.getText();
    phoneNumber = phoneNumberField.getText();
    birthDate = dateOfBirthField.getValue();
    address = addressField.getText();

    // Dùng .trim().isEmpty() để kiểm tra chuỗi rỗng hoặc chỉ toàn dấu cách
    boolean isRealNameValid = realName != null && !realName.trim().isEmpty();
    boolean isEmailValid = email != null && !email.trim().isEmpty();
    boolean isPhoneValid = phoneNumber != null && !phoneNumber.trim().isEmpty();
    boolean isAddressValid = address != null && !address.trim().isEmpty();
    boolean isDateValid = birthDate != null;

    if (isRealNameValid && isEmailValid && isPhoneValid && isDateValid && isAddressValid) {
      return true;
    }

    return false;
  }

  @FXML
  public void handleUpdateProfile() {
    if (isInformationValid()) {
      String userId = SessionManager.getCurrentUser().getId();
      UpdateProfileRequestDTO updateProfileReq = new UpdateProfileRequestDTO(userId,
                                                                             realName,
                                                                             email,
                                                                             phoneNumber,
                                                                             birthDate,
                                                                             address);
      ServerConnection.sendData(updateProfileReq);
    } else {
      ScreenController.showAlert(Alert.AlertType.WARNING,
          "Cảnh báo", "Vui lòng nhập đầy đủ thông tin");
      return;
    }
  }

  @FXML
  public void gotoWallet() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Bạn chưa lưu thay đổi",
        "Bạn có chắc chắn muốn chuyển sang trang Ví người dùng không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        homeController.gotoWallet();
      }
    });
  }

  @FXML
  public void gotoResult() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Bạn chưa lưu thay đổi",
        "Bạn có chắc chắn muốn chuyển sang trang Kết quả đấu giá không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        homeController.gotoResult();
      }
    });
  }

  @FXML
  public void handleChooseImage() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Chọn ảnh đại diện");
    FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg");
    fileChooser.getExtensionFilters().add(imageFilter);

    File file = fileChooser.showOpenDialog(ScreenController.primaryStage);

    if (file != null) {
      selectedImageFile = file;

      Image image = new Image(file.toURI().toString());

      imageViewField.setImage(image);
    }
  }

  @FXML
  public void gotoHomeWithHyperLink() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Bạn chưa lưu thay đổi",
        "Bạn có chắc chắn muốn về Trang chủ không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        ScreenController.switchScreen("Home.fxml", "Trang chủ");
      }
    });
  }
}
