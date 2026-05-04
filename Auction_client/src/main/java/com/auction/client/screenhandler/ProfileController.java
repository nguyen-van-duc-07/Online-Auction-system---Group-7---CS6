package com.auction.client.screenhandler;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;

/**
 * Controller xử lý logic cho màn hình cập nhật thông tin tài khoảnkhoản.
 */
public class ProfileController {
  private File selectedImageFile;

  HomeController homeController = new HomeController();

  @FXML
  private TextField userNameField;
  @FXML
  private TextField emailField;
  @FXML
  private DatePicker dateOfBirthField;
  @FXML
  private TextField phoneNumberField;
  @FXML
  private ImageView imageViewField;

  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Bạn chưa lưu thay đổi",
        "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        homeController.gotoLogin();
      }
    });
  }

  @FXML
  public void gotoHome() {
    if (handleUpdateInformation()) {
      ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thông báo",
          "Cập nhật dữ liệu thành công").ifPresent(Response -> {
        if (Response == ButtonType.OK) {
          ScreenController.switchScreen("Home.fxml", "Trang chủ");
        }
      });
    } else {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Thông báo",
          "Vui lòng điền đầy đủ thông tin");
      return;
    }
  }

  public boolean handleUpdateInformation() {
    String userName = userNameField.getText();
    String email = emailField.getText();
    String phoneNumber = phoneNumberField.getText();
    LocalDate birthDate = dateOfBirthField.getValue();

    // Dùng .trim().isEmpty() để kiểm tra chuỗi rỗng hoặc chỉ toàn dấu cách
    boolean isUserNameValid = userName != null && !userName.trim().isEmpty();
    boolean isEmailValid = email != null && !email.trim().isEmpty();
    boolean isPhoneValid = phoneNumber != null && !phoneNumber.trim().isEmpty();
    boolean isDateValid = birthDate != null;

    if (isUserNameValid && isEmailValid && isPhoneValid && isDateValid) {
      return true;
    }

    return false;
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
}
