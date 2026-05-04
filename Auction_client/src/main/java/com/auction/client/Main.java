package com.auction.client;

import com.auction.client.network.ServerConnection;
import com.auction.client.screenhandler.ScreenController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    // Đặt mặc định cho ScreenController biết rằng đây là stage chính của chương trình
    ScreenController.primaryStage = stage;
    ScreenController.switchScreen("Login.fxml", "Đăng nhập");
    ServerConnection.connect();
  }

  public static void main(String[] args) {
    launch();
  }
}