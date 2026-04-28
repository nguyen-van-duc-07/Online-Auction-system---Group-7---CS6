package com.auction.client;

import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.User;
import javafx.application.Application;
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
        ScreenController.switchScreen(null, "Login.fxml", "Đăng nhập");
        userDatabase.put("123", new Bidder());
    }

    public static void main(String[] args) {
        launch();
    }
}