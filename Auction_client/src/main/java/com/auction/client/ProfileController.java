package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

public class ProfileController
{
    HomeController homeController = new HomeController();

    @FXML private TextField userNameField;
    @FXML private TextField emailField;
    @FXML private DatePicker dateOfBirthField;
    @FXML private TextField phoneNumberField;

    @FXML
    public void gotoLogin(ActionEvent event) {
        homeController.gotoLogin(event);
    }

    @FXML
    public void gotoHome(ActionEvent event) {
        if (handleUpdateInformation()) {
            ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thông báo",
                    "Cập nhật dữ liệu thành công", event).ifPresent(Response -> {
                if (Response == ButtonType.OK) {
                    ScreenController.switchScreen(event, "Home.fxml", "Trang chủ");
                }
            });
        }
        else {
            ScreenController.showAlert(Alert.AlertType.WARNING, "Thông báo",
                    "Vui lòng điền đầy đủ thông tin", event);
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
    public void gotoWallet(ActionEvent event) {
        homeController.gotoWallet(event);
    }

    @FXML
    public void gotoResult(ActionEvent event) {
        homeController.gotoResult(event);
    }
}
