package com.auction.client;

import com.auction.client.network.ServerConnection;
import com.auction.client.screenhandler.ScreenController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    // Đặt mặc định cho ScreenController biết rằng đây là stage chính của chương trình
    ScreenController.primaryStage = stage;

    FXMLLoader fxmlLoader = new FXMLLoader();
    Parent root = fxmlLoader.load(getClass().getResource("/com/auction/client/User/Login.fxml"));
    Scene scene = new Scene(root);
    stage.setScene(scene);
    stage.setTitle("Đăng nhập");

    stage.show();

    ServerConnection.connect();
  }

  public static void main(String[] args) {
    launch();
  }
}