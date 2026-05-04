package com.auction.client.screenhandler;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

/**
 * Controller xử lý logic cho màn hình cập nhật thông tin tài khoảnkhoản.
 */
public class ProfileController {
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
  public void gotoLogin() {
    homeController.gotoLogin();
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
    homeController.gotoWallet();
  }

  @FXML
  public void gotoResult() {
    homeController.gotoResult();
  }
}
