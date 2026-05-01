package com.auction.client;

import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
@Setter

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
        // Cách 1: Dùng Builder (Khuyên dùng vì UserDTO có @SuperBuilder)
        UserDTO dto = UserDTO.builder()
                .userName("123")
                .password("123")
                .role(UserRole.BIDDER) // Đảm bảo UserRole đã được import
                .build();
        Bidder adminBidder = new Bidder(dto);
        userDatabase.put("123", adminBidder);
    }

    public static void main(String[] args) {
        launch();
    }
}