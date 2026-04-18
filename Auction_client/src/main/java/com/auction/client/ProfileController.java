package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

public class ProfileController
{
    @FXML private TextField userNameField;
    @FXML private TextField emailField;
    @FXML private DatePicker dateOfBirthField;
    @FXML private TextField phoneNumberField;

    @FXML
    public void gotoLogin(ActionEvent event)
    {
        ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Xác nhận đăng xuất",
                "Bạn có chắc chắn muốn đăng xuất không?", event).ifPresent(Response -> {
                    if (Response == ButtonType.OK)
                    {
                        ScreenController.switchScreen(event, "Login.fxml", "Đăng nhập");
                    }
        });
    }

    @FXML
    public void gotoHome(ActionEvent event)
    {
        ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thông báo",
                "Cập nhật dữ liệu thành công", event).ifPresent(Response -> {
            if (Response == ButtonType.OK)
            {
                ScreenController.switchScreen(event, "Home.fxml", "Trang chủ");
            }
        });
    }

    @FXML
    public void handleUpdateInformation()
    {
        String userName = userNameField.getText();
        String email = emailField.getText();
        String phoneNumber = phoneNumberField.getText();
        LocalDate birthDate = dateOfBirthField.getValue();
    }

    @FXML
    public void gotoWallet(ActionEvent event)
    {
        ScreenController.switchScreen(event, "Wallet.fxml", "Ví người dùng");
    }
}
