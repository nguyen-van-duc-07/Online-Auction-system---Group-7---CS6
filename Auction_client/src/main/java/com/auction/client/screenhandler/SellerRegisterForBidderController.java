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

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    locationField.setText(SessionManager.getCurrentUser().getAddress());
  }

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

  @FXML
  public void gotoHome() {
    if (isInformationValid()) {

    } else {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Chưa điền đầy đủ thông tin!",
          "Bạn chắc chắn muốn trở về chứ?").ifPresent(Response -> {
            if (Response == ButtonType.OK) {
              ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
            }
      });
    }
  }

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
