package com.auction.client.screenhandler;

import com.auction.client.network.SessionManager;
import com.auction.client.screenhandler.admin.AdminScreenController;
import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.UserDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Bộ điều khiển (Controller) cho màn hình thông tin tài khoản người dùng (Profile).
 * Hiển thị các thông tin chi tiết cá nhân như họ tên, địa chỉ, ngày sinh, email và số điện thoại.
 */
public class ProfileController implements Initializable {

  @FXML
  private Label accountNameLabel;
  @FXML
  private Label addressLabel;
  @FXML
  private Label dobLabel;
  @FXML
  private Label emailLabel;
  @FXML
  private Label phoneNumberLabel;

  /**
   * Hàm được tự động gọi khi màn hình Profile.fxml được nạp lên giao diện.
   * Truy xuất dữ liệu từ {@link SessionManager} và điền tự động vào các nhãn hiển thị.
   *
   * @param location vị trí đường dẫn tương đối của đối tượng gốc
   * @param resources tài nguyên sử dụng để bản địa hóa đối tượng gốc
   */
  public void initialize(URL location, ResourceBundle resources) {
    UserDTO currentUser = SessionManager.getCurrentUser();

    if (currentUser != null) {
      if (currentUser.getAccountName() != null) {
        if (currentUser.getAccountName() != null) {
          accountNameLabel.setText(currentUser.getAccountName());
        }
        if (currentUser.getEmail() != null) {
          emailLabel.setText(currentUser.getEmail());
        }
        if (currentUser.getPhoneNumber() != null) {
          phoneNumberLabel.setText(currentUser.getPhoneNumber());
        }
        if (currentUser.getDob() != null) {
          LocalDate dob = currentUser.getDob();
          String dobString = dob.getDayOfMonth() + " Tháng " + dob.getMonthValue() + " Năm " + dob.getYear();
          dobLabel.setText(dobString);
        }
        if (currentUser.getAddress() != null) {
          addressLabel.setText(currentUser.getAddress());
        }
      }
    }
  }

  /**
   * Chuyển hướng người dùng sang giao diện chỉnh sửa thông tin cá nhân.
   */
  @FXML
  public void gotoEditProfile() {
    AdminScreenController adminController = AdminScreenController.getInstance();
    if (adminController != null) {
      adminController.loadComponent("/com/auction/client/User/EditProfile.fxml");
    } else {
      MainLayoutController mainLayoutController = MainLayoutController.getInstance();
      if (mainLayoutController != null) {
        mainLayoutController.loadComponent("/com/auction/client/User/EditProfile.fxml");
      }
    }
  }
}
