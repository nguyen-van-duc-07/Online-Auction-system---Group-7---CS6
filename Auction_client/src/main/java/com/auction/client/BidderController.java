package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BidderController implements Initializable {

    // Khai báo biến đại diện cho danh sách trên giao diện
    @FXML private ListView<String> listProducts;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Thêm dữ liệu giả vào danh sách cho người mua xem
        listProducts.getItems().addAll(
                "🔥 [Đang đấu giá] iPhone 15 Pro Max - Giá hiện tại: 25.000.000 VNĐ",
                "🔥 [Đang đấu giá] Laptop MacBook Pro M3 - Giá hiện tại: 32.000.000 VNĐ",
                "⏳ [Sắp mở] Đồng hồ Rolex - Giá khởi điểm: 150.000.000 VNĐ",
                "✅ [Đã kết thúc] Giày Nike Air Jordan - Người thắng: admin"
        );
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Đăng xuất");
        alert.setHeaderText("Bạn đang cố gắng đăng xuất khỏi tài khoản...");
        alert.setContentText("Bạn có muốn tiếp tục không?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            goToLogin(event);
        }
    }

    private void goToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}