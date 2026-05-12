package com.auction.client.screenhandler;

import java.io.IOException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

/**
 * Class có nhiệm vụ quản lý màn hình.
 */
public class ScreenController {
  public static Stage primaryStage;

  // Dùng để chuyển sang trang bất kì hiệu quả hơn
  public static void switchScreen(String fxmlFile, String title) {
    try {
      Parent root = FXMLLoader.load(ScreenController.class.getResource("/com/auction/client/" + fxmlFile));
      Scene scene = new Scene(root);

      primaryStage.setScene(scene);
      primaryStage.setTitle(title);

      primaryStage.setResizable(false); // Khoá tính năng thay đổi kích thước của cửa sổ

      primaryStage.show();

    } catch (Exception e) {
      e.printStackTrace();
      showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình: " + fxmlFile);
    }
  }

  // Dùng để tạo ra cảnh báo
  public static Optional<ButtonType> showAlert(Alert.AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.initOwner(primaryStage);

    return alert.showAndWait();
  }

  // Dùng để tạo ra cửa sổ con mới, thiết lập quan hệ cha con, khi cửa sổ cha đóng thì cửa sổ con cũng đóng theo
  public static void createSubWindow(String fxmlFile, String title) {
    try {
      Parent root = FXMLLoader.load(ScreenController.class.getResource("/com/auction/client/" + fxmlFile));
      Scene scene = new Scene(root);
      // Tạo cửa sổ con mới
      Stage newStage = new Stage();
      newStage.setTitle(title);
      newStage.setScene(scene);


      // Xác định mối quan hệ cha con giữa 2 cửa sổ
      newStage.initOwner(primaryStage);
      // Ngăn người dùng tương tác với cửa sổ cũ khi cửa sổ mới đang mở (tùy chọn)
      newStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

      // Tạm ẩn cửa sổ đi (độ mờ = 0) để tránh hiệu ứng "nháy" vị trí
      newStage.setOpacity(0);

      // Lắng nghe sự kiện ngay khi OS vừa cấp phát xong kích thước thực tế
      newStage.setOnShown(event -> {
        // Tính điểm chính giữa của cửa sổ cha
        double centerXPosition = primaryStage.getX() + primaryStage.getWidth() / 2;
        double centerYPosition = primaryStage.getY() + primaryStage.getHeight() / 2;

        // Dịch chuyển cửa sổ con sao cho tâm của nó trùng với tâm cửa sổ cha
        newStage.setX(centerXPosition - newStage.getWidth() / 2);
        newStage.setY(centerYPosition - newStage.getHeight() / 2);

        // Hiển thị lại cửa sổ rõ ràng sau khi đã vào đúng vị trí
        newStage.setOpacity(1);
      });

      newStage.setResizable(false); // Khoá tính năng thay đổi kích thước của cửa sổ phụ
      newStage.show();
    } catch (IOException e) {
      e.printStackTrace();
      showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình" + fxmlFile);
    }
  }
}