package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.client.screenhandler.admin.AdminScreenController;
import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.UpdateProfileRequestDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

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

  private String accountName;
  private String email;
  private String phoneNumber;
  private String address;
  private LocalDate birthDate;

  @FXML
  private TextField accountNameField;
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
        if (currentUser.getAccountName() != null) {
          accountNameField.setText(currentUser.getAccountName());
        }
        if (currentUser.getEmail() != null) {
          emailField.setText(currentUser.getEmail());
        }
        if (currentUser.getPhoneNumber() != null) {
          phoneNumberField.setText(currentUser.getPhoneNumber());
          phoneNumberField.setDisable(true);
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
    accountName = accountNameField.getText();
    email = emailField.getText();
    phoneNumber = phoneNumberField.getText();
    birthDate = dateOfBirthField.getValue();
    address = addressField.getText();

    // Dùng .trim().isEmpty() để kiểm tra chuỗi rỗng hoặc chỉ toàn dấu cách
    boolean isRealNameValid = accountName != null && !accountName.trim().isEmpty();
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
    accountName = accountNameField.getText();
    email = emailField.getText();
    birthDate = dateOfBirthField.getValue();
    address = addressField.getText();

    // Kiểm tra xem các Field có thay đổi nào không
    boolean isRealNameChanged = !(accountName.equals(currentUser.getAccountName()));
    boolean isEmailChanged = !(email.equals(currentUser.getEmail()));
    boolean isAddressChanged = !(address.equals(currentUser.getAddress()));
    boolean isDateChanged = !(birthDate.equals(currentUser.getDob()));

    if (isRealNameChanged || isEmailChanged || isAddressChanged || isDateChanged) {
      return true;
    } else {
      return false;
    }
  }

  @FXML
  public void handleUpdateProfile() {
    if (isInformationValid() && isInformationChanged()) {
      String userId = currentUser.getId();
      UpdateProfileRequestDTO updateProfileReq = new UpdateProfileRequestDTO(
          userId,
          accountName,
          email,
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
    AdminScreenController adminController = AdminScreenController.getInstance();
    if (adminController != null) {
      if (isInformationChanged()) {
        ScreenController.showAlert(Alert.AlertType.WARNING, "Có thay đổi chưa được lưu",
                "Bạn có chắc chắn muốn huỷ bỏ không?").ifPresent(Response -> {
          if (Response == ButtonType.OK) {
            adminController.gotoProfile();
          }
        });
      } else {
        adminController.gotoProfile();
      }
      return;
    }

    MainLayoutController mainLayoutController = MainLayoutController.getInstance();
    if (mainLayoutController != null) {
      if (isInformationChanged()) {
        ScreenController.showAlert(Alert.AlertType.WARNING, "Có thay đổi chưa được lưu",
                "Bạn có chắc chắn muốn huỷ bỏ không?").ifPresent(Response -> {
          if (Response == ButtonType.OK) {
            mainLayoutController.loadComponent("/com/auction/client/User/Profile.fxml");
          }
        });
      } else {
        mainLayoutController.loadComponent("/com/auction/client/User/Profile.fxml");
      }
    }
  }
}
