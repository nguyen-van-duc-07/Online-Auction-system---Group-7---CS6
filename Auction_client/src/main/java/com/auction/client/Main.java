package com.auction.client;

import com.auction.client.network.ServerConnection;
import com.auction.client.screenhandler.ScreenController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Lớp khởi chạy chính của ứng dụng đấu giá trực tuyến phía Client.
 * <p>
 * Lớp này chịu trách nhiệm khởi tạo giao diện người dùng JavaFX, thiết lập múi giờ mặc định,
 * quản lý vòng đời ứng dụng và kết nối ban đầu tới máy chủ đấu giá.
 * </p>
 */
public class Main extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    // Đặt mặc định cho ScreenController biết rằng đây là stage chính của chương trình
    ScreenController.primaryStage = stage;

    ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");

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

    try {
      ServerConnection.connect();
    } catch (Exception e) {
      // Bỏ qua hoặc log lỗi kết nối khi khởi động, cho phép tự kết nối lại khi thao tác đăng nhập
    }
  }

  @Override
  public void stop() throws Exception {
    ServerConnection.closeConnection();
    super.stop();
  }

  /**
   * Phương thức khởi chạy ứng dụng chính.
   *
   * @param args các tham số dòng lệnh truyền vào từ JVM
   */
  public static void main(String[] args) {
    // Đảm bảo Client JVM chạy trên múi giờ Việt Nam (GMT+7)
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    launch();
  }
}