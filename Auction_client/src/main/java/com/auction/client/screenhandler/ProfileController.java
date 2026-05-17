package com.auction.client.screenhandler;

import com.auction.client.network.SessionManager;
import com.auction.shared.model.user.UserDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML
    private Label realNameLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Label dobLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label phoneNumberLabel;

    /**
     * Hàm được tự động gọi khi màn hình Profile.fxml được load lên.
     *
     * <p>Truy xuất dữ liệu từ {@link SessionManager} và điền tự động vào các Label.</p>
     */
    public void initialize(URL location, ResourceBundle resources) {
        UserDTO currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            if (currentUser.getAccountName() != null) {
                if (currentUser.getRealName() != null) {
                    realNameLabel.setText(currentUser.getRealName());
                }
                if (currentUser.getEmail() != null) {
                    emailLabel.setText(currentUser.getEmail());
                }
                if (currentUser.getPhoneNumber() != null) {
                    phoneNumberLabel.setText(currentUser.getPhoneNumber());
                }
                if (currentUser.getDob() != null) {
                    LocalDate dob = currentUser.getDob();
                    String dobString = dob.getDayOfMonth() + " Tháng " + dob.getMonthValue() + " Năm " +  dob.getYear();
                    dobLabel.setText(dobString);
                }
                if (currentUser.getAddress() != null) {
                    addressLabel.setText(currentUser.getAddress());
                }
            }
        }
    }

    @FXML
    public void gotoEditProfile() {
        HomeController homeController = HomeController.getInstance();
        homeController.loadComponent("/com/auction/client/User/EditProfile.fxml");
    }
}
