package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import com.auction.shared.model.User;

public class LoginController {

    @FXML private TextField txtLoginUser;
    @FXML private PasswordField hiddenPasswordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Label eyeIconShow;
    @FXML private Label eyeIconHide;

    private boolean isPasswordVisible = false;

    @FXML
    private void togglePasswordVisible(MouseEvent event) {
        // Chuyển sang trạng thái ngược lại
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            // Lấy String từ hiddenPasswordField rồi set cho visiblePasswordField
            visiblePasswordField.setText(hiddenPasswordField.getText());
            visiblePasswordField.setVisible(true);
            hiddenPasswordField.setVisible(false);
            eyeIconHide.setVisible(true);
            eyeIconShow.setVisible(false);
        }
        else {
            hiddenPasswordField.setText(visiblePasswordField.getText());
            hiddenPasswordField.setVisible(true);
            visiblePasswordField.setVisible(false);
            eyeIconHide.setVisible(false);
            eyeIconShow.setVisible(true);
        }
    }
    // Logic đăng nhập
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtLoginUser.getText();
        String password = isPasswordVisible ? visiblePasswordField.getText() : hiddenPasswordField.getText();

        // 1. Kiểm tra xem người dùng đã nhập đủ thông tin chưa
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu!");
            return;
        }

        // 2. Kiểm tra xem tài khoản có tồn tại trong bộ nhớ tạm chưa
        if (!Main.userDatabase.containsKey(username)) {
            // NẾU CHƯA CÓ TÀI KHOẢN
            showAlert("Chưa có tài khoản!");
            return;
        }

        // 3. Nếu tài khoản có tồn tại, kiểm tra xem mật khẩu có khớp không
        if (!Main.userDatabase.get(username).getPassword().equals(password)) {
            // NẾU SAI MẬT KHẨU
            showAlert("Mật khẩu không chính xác!");
            return;
        }

        // 4. Vượt qua hết các lỗi trên -> Đăng nhập thành công!
        try {
            // Lấy vai trò của user này từ bộ nhớ tạm
            String role = Main.userRoles.get(username);

            // Nếu là Bidder thì mở bidder.fxml, nếu là Seller thì mở seller.fxml
            String NEXT_SCREEN_FXML = role.equals("Bidder") ? "bidder.fxml" : "seller.fxml";

            Parent root = FXMLLoader.load(getClass().getResource(NEXT_SCREEN_FXML));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Trang chủ - " + role);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi: Không tải được giao diện trang chủ!");
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Signup.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Đăng ký");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}