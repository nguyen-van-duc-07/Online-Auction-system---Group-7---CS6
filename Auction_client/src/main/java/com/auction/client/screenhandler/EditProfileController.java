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
public class EditProfileController implements Initializable {
  private final UserDTO currentUser = SessionManager.getCurrentUser();

  private String realName;
  private String email;
  private String phoneNumber;
  private String address;
  private LocalDate birthDate;

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

  /**
   * Hàm được tự động gọi khi màn hình Profile.fxml được load lên.
   *
   * <p>Truy xuất dữ liệu từ {@link SessionManager} và điền tự động vào các TextField.</p>
   */
  public void initialize(URL location, ResourceBundle resources) {
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

  public boolean isInformationChanged() {
    realName = realNameField.getText();
    email = emailField.getText();
    phoneNumber = phoneNumberField.getText();
    birthDate = dateOfBirthField.getValue();
    address = addressField.getText();

    // Kiểm tra xem các Field có thay đổi nào không
    boolean isRealNameChanged = !(realName.equals(currentUser.getRealName()));
    boolean isEmailChanged = !(email.equals(currentUser.getEmail()));
    boolean isPhoneChanged = !(phoneNumber.equals(currentUser.getPhoneNumber()));
    boolean isAddressChanged = !(address.equals(currentUser.getAddress()));
    boolean isDateChanged = !(birthDate.equals(currentUser.getDob()));

    if (isRealNameChanged || isEmailChanged || isPhoneChanged || isAddressChanged || isDateChanged) {
      return true;
    } else {
      return false;
    }
  }

  @FXML
  public void handleUpdateProfile() {
    if (isInformationValid() && isInformationChanged()) {
      String userId = currentUser.getId();
      UpdateProfileRequestDTO updateProfileReq = new UpdateProfileRequestDTO(userId,
                                                                             realName,
                                                                             email,
                                                                             phoneNumber,
                                                                             birthDate,
                                                                             address);
      ServerConnection.sendData(updateProfileReq);
    } else if (!isInformationValid()) {
      ScreenController.showAlert(Alert.AlertType.WARNING,
          "Cảnh báo", "Vui lòng nhập đầy đủ thông tin!");
      return;
    } else {
      ScreenController.showAlert(Alert.AlertType.INFORMATION,
              "Thông báo", "Không có thay đổi nào được lưu!");
      return;
    }
  }

  @FXML
  public void handleCancel() {
    HomeController homeController = HomeController.getInstance();
    if (isInformationChanged()) {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Có thay đổi chưa được lưu",
              "Bạn có chắc chắn muốn huỷ bỏ không?").ifPresent(Response -> {
        if (Response == ButtonType.OK) {
          homeController.loadComponent("/com/auction/client/User/Profile.fxml");
        }
      });
    } else {
      homeController.loadComponent("/com/auction/client/User/Profile.fxml");
    }
  }
}
