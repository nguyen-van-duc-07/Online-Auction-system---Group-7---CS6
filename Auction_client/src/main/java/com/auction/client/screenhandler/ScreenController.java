package com.auction.client.screenhandler;

import java.io.IOException;
import java.util.Optional;
import java.util.Stack;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Class có nhiệm vụ quản lý màn hình.
 */
public class ScreenController {
  public static Stage primaryStage;

  private static final Stack<ScreenState> history = new Stack<ScreenState>();
  private static ScreenState currentScreen = null;

  // Dùng để chuyển sang trang bất kì hiệu quả hơn
  public static void switchScreen(String fxmlFile, String title) {
    try {
      // Nếu có màn hình hiện tại, lưu lại chính Node hiện tại vào lịch sử.
      if (primaryStage.getScene() != null && currentScreen != null) {
        Parent currentRoot = primaryStage.getScene().getRoot();
        history.push(new ScreenState(currentRoot, currentScreen.getTitle(),
            currentScreen.getFxmlFile()));
      }

      // Load màn hình mới từ FXML
      Parent root = FXMLLoader.load(ScreenController.class.getResource(
          "/com/auction/client/" + fxmlFile));
      currentScreen = new ScreenState(root, title, fxmlFile);

      // Hiển thị lên màn hình
      primaryStage.getScene().setRoot(root);
      primaryStage.setTitle(title);
      
      if (fxmlFile.contains("Login.fxml") || fxmlFile.contains("SignUp.fxml")) {
        primaryStage.setMaximized(false);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
      } else {
        primaryStage.setMaximized(true);
        primaryStage.setResizable(false);
      }
      
      primaryStage.show();

    } catch (Exception e) {
      e.printStackTrace();
      showAlert(Alert.AlertType.ERROR,
          "Lỗi hệ thống", "Không thể tải màn hình: " + fxmlFile);
    }
  }

  // Phương thức Quay lại tối ưu
  public static void goBack() {
    if (!history.isEmpty()) {
      // Lấy trạng thái màn hình cũ ra
      ScreenState previous = history.pop();
      currentScreen = previous;
      // Không cần load FXML: Gắn lại trực tiếp Root cũ vào Scene
      primaryStage.getScene().setRoot(previous.getRoot());
      primaryStage.setTitle(previous.getTitle());
      
      if (previous.getFxmlFile().contains("Login.fxml") || previous.getFxmlFile().contains("SignUp.fxml")) {
        primaryStage.setMaximized(false);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
      } else {
        primaryStage.setMaximized(true);
        primaryStage.setResizable(false);
      }
      
      primaryStage.show();

      System.out.println("Quay lại màn hình trước đó thành công");
    } else {
      // Fallback về trang chủ nếu không có lịch sử
      switchScreen("Bidder/Home.fxml", "Trang chủ");
    }
  }

  // Sử dụng khi đăng xuất
  public static void clearHistory() {
    history.clear();
    currentScreen = null;
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

  // Dùng để tạo ra cửa sổ con mới VÀ trả về Controller của màn hình đó để xử lý logic (như truyền Callback)
  public static <T> T createSubWindowAndGetController(String fxmlFile, String title) {
    try {
      FXMLLoader loader = new FXMLLoader(ScreenController.class.getResource("/com/auction/client/" + fxmlFile));
      Parent root = loader.load();
      Scene scene = new Scene(root);

      Stage newStage = new Stage();
      newStage.setTitle(title);
      newStage.setScene(scene);

      newStage.initOwner(primaryStage);
      newStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      newStage.setOpacity(0);

      newStage.setOnShown(event -> {
        double centerXPosition = primaryStage.getX() + primaryStage.getWidth() / 2;
        double centerYPosition = primaryStage.getY() + primaryStage.getHeight() / 2;

        newStage.setX(centerXPosition - newStage.getWidth() / 2);
        newStage.setY(centerYPosition - newStage.getHeight() / 2);
        newStage.setOpacity(1);
      });

      newStage.setResizable(false);
      newStage.show();

      return loader.getController();

    } catch (IOException e) {
      e.printStackTrace();
      showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình: " + fxmlFile);
      return null;
    }
  }
}