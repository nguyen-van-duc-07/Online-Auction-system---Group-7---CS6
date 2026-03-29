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

public class SellerController implements Initializable {

    // Khai báo biến đại diện cho danh sách trên giao diện
    @FXML private ListView<String> listProducts;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Thêm dữ liệu giả vào danh sách cho người bán quản lý
        listProducts.getItems().addAll(
                "📦 [Của bạn] Xe máy Honda SH 150i - Lượt trả giá: 5",
                "📦 [Của bạn] Bức tranh phong cảnh - Lượt trả giá: 12",
                "📦 [Của bạn] Máy ảnh Sony A7IV - Lượt trả giá: 0"
        );
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Logout");
        alert.setHeaderText("You're trying to logout...");
        alert.setContentText("Do you want to continue");
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("deptrai1.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);

                stage.setScene(scene);
                stage.setTitle("Đăng nhập");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}