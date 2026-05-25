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

    // Lắng nghe sự kiện đóng cửa sổ chính để tắt kết nối và thoát tiến trình sạch sẽ
    stage.setOnCloseRequest(event -> {
      try {
        stop();
      } catch (Exception e) {
        // Bỏ qua lỗi khi đóng ứng dụng
      }
      System.exit(0);
    });

    stage.show();

    ServerConnection.connect();
  }

  @Override
  public void stop() throws Exception {
    ServerConnection.closeConnection();
    super.stop();
  }

  public static void main(String[] args) {
    // Đảm bảo Client JVM chạy trên múi giờ Việt Nam (GMT+7)
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    launch();
  }
}