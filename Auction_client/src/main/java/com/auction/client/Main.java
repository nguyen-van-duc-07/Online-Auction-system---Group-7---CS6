package com.auction.client;

import com.auction.shared.model.Bidder;
import com.auction.shared.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application
{

    // 1. LƯU TÀI KHOẢN VÀ MẬT KHẨU
    public static Map<String, User> userDatabase = new HashMap<>();

    // 2. LƯU TÀI KHOẢN VÀ VAI TRÒ (Bidder/Seller)
    public static Map<String, String> userRoles = new HashMap<>();

    @Override
    public void start(Stage stage) throws Exception
    {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("Login.fxml")));
        stage.setScene(scene);
        stage.setTitle("Đăng nhập");
        stage.show();
        userDatabase.put("123", new Bidder("123", "123"));
    }

    public static void main(String[] args) {
        launch();
    }
}