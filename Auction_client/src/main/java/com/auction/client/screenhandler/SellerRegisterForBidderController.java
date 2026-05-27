package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.enums.SellerRegisterStatus;
import com.auction.shared.request.SellerRegisterRequestDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Bộ điều khiển (Controller) cho màn hình đăng ký tài khoản người bán (SellerRegister).
 * Cho phép người mua điền thông tin thương hiệu, căn cước công dân và tài khoản ngân hàng để nâng cấp tài khoản.
 */
public class SellerRegisterForBidderController implements Initializable {
  private String brandName;
  private String citizenIdentityCard;
  private String location;
  private String bankAccount;
  private String bankName;
  @FXML
  private TextField brandNameField;
  @FXML
  private TextField citizenIdentityCardField;
  @FXML
  private TextField locationField;
  @FXML
  private TextField bankAccountField;
  @FXML
  private TextField bankNameField;

  /**
   * Khởi tạo bộ điều khiển đăng ký người bán.
   * Tự động thiết lập địa chỉ mặc định của người dùng từ Session.
   *
   * @param location vị trí đường dẫn tương đối của đối tượng gốc
   * @param resources tài nguyên sử dụng để bản địa hóa đối tượng gốc
   */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    locationField.setText(SessionManager.getCurrentUser().getAddress());
  }

  /**
   * Kiểm tra xem các trường thông tin đăng ký nhập vào có đầy đủ và hợp lệ hay không.
   *
   * @return {@code true} nếu thông tin hợp lệ, ngược lại {@code false}
   */
  public boolean isInformationValid() {
    brandName = brandNameField.getText();
    citizenIdentityCard = citizenIdentityCardField.getText();
    location = locationField.getText();
    bankAccount = bankAccountField.getText();
    bankName = bankNameField.getText();

    // Kiểm tra xem các phần TextField đã được điền hay chưa
    boolean isBrandNameValid = brandName != null && !brandName.trim().isEmpty();
    boolean isCitizenIdentityCardValid = citizenIdentityCard != null && !citizenIdentityCard.trim().isEmpty();
    boolean isLocationValid = location != null && !location.trim().isEmpty();
    boolean isBankAccountValid = bankAccount != null && !bankAccount.trim().isEmpty();
    boolean isBankNameValid = bankName != null && !bankName.trim().isEmpty();

    if  (isBrandNameValid && isCitizenIdentityCardValid
        && isLocationValid &&  isBankAccountValid && isBankNameValid) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Quay trở lại màn hình trang chủ của người mua.
   * Cảnh báo người dùng nếu có dữ liệu chưa điền đầy đủ trước khi thoát.
   */
  @FXML
  public void gotoHome() {
    if (isInformationValid()) {

    } else {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Chưa điền đầy đủ thông tin!",
          "Bạn chắc chắn muốn trở về chứ?").ifPresent(Response -> {
            if (Response == ButtonType.OK) {
              MainLayoutController.getInstance().gotoHomeFeed();
            }
      });
    }
  }

  /**
   * Xử lý hành động gửi đơn đăng ký nâng cấp tài khoản người bán lên Server.
   */
  @FXML
  public void handleRegister() {
    if (!isInformationValid()) {
      ScreenController.showAlert(Alert.AlertType.WARNING,
          "Thông báo", "Vui lòng điền đầy đủ thông tin");
      return;
    } else {
      SellerRegisterRequestDTO sellerRegisterReq = new SellerRegisterRequestDTO();
      sellerRegisterReq.setUserId(SessionManager.getCurrentUser().getId());
      sellerRegisterReq.setBrandName(brandName);
      sellerRegisterReq.setCitizenIdentityCard(citizenIdentityCard);
      sellerRegisterReq.setLocation(location);
      sellerRegisterReq.setBankAccount(bankAccount);
      sellerRegisterReq.setBankName(bankName);
      ServerConnection.sendData(sellerRegisterReq);
    }
  }
}
