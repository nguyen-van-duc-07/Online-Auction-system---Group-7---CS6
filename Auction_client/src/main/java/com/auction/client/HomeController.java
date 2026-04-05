package com.auction.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController
{
    @FXML
    public void gotoLogin(ActionEvent event)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất không?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK)
            {
                try
                {
                    Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
                    Stage stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Đăng nhập");
                    stage.show();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
    @FXML
    public void gotoSellerHome(ActionEvent event)
    {
        try
        {
            Parent root = FXMLLoader.load(getClass().getResource("SellerHome.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Quản lý sản phẩm (Seller)");
            stage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}