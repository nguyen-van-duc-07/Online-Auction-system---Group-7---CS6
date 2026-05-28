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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quản lý các màn hình và trạng thái điều hướng trong ứng dụng JavaFX.
 * Lớp này chịu trách nhiệm chuyển đổi màn hình, lưu lịch sử để hỗ trợ chức năng quay lại (back),
 * hiển thị các cửa sổ con (sub-window) và các hộp thoại cảnh báo (alert).
 */
public class ScreenController {
  private static final Logger log = LoggerFactory.getLogger(ScreenController.class);

  /**
   * Cửa sổ chính (Stage) của ứng dụng JavaFX.
   */
  public static Stage primaryStage;

  /**
   * Constructor private để ngăn khởi tạo đối tượng của lớp quản lý màn hình tĩnh.
   */
  private ScreenController() {
    // Ngăn khởi tạo lớp tiện ích/quản lý tĩnh
  }

  private static final Stack<ScreenState> history = new Stack<ScreenState>();
  private static ScreenState currentScreen = null;

  /**
   * Chuyển sang màn hình bất kỳ một cách hiệu quả dựa trên tệp FXML và tiêu đề.
   *
   * @param fxmlFile đường dẫn tệp FXML của màn hình cần chuyển đổi
   * @param title tiêu đề hiển thị trên thanh tiêu đề của cửa sổ
   */
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
      if (primaryStage.getScene() == null) {
        primaryStage.setScene(new Scene(root));
      } else {
        primaryStage.getScene().setRoot(root);
      }
      primaryStage.setTitle(title);
      
      if (fxmlFile.contains("Login.fxml") || fxmlFile.contains("SignUp.fxml")) {
        primaryStage.setMaximized(false);
        primaryStage.setResizable(true);
        javafx.application.Platform.runLater(() -> {
          primaryStage.sizeToScene();
          primaryStage.centerOnScreen();
          primaryStage.setResizable(false);
        });
      } else {
        primaryStage.setMaximized(true);
        primaryStage.setResizable(false);
      }
      
      primaryStage.show();

    } catch (Exception e) {
      log.error("Lỗi hệ thống khi tải màn hình: {}", fxmlFile, e);
      showAlert(Alert.AlertType.ERROR,
          "Lỗi hệ thống", "Không thể tải màn hình: " + fxmlFile);
    }
  }

  /**
   * Quay lại màn hình trước đó trong lịch sử duyệt màn hình (tối ưu không tải lại FXML).
   * Nếu lịch sử rỗng, màn hình sẽ tự động chuyển về trang chủ (Bidder Home).
   */
  public static void goBack() {
    if (!history.isEmpty()) {
      // Lấy trạng thái màn hình cũ ra
      ScreenState previous = history.pop();
      currentScreen = previous;
      // Không cần load FXML: Gắn lại trực tiếp Root cũ vào Scene
      if (primaryStage.getScene() == null) {
        primaryStage.setScene(new Scene(previous.getRoot()));
      } else {
        primaryStage.getScene().setRoot(previous.getRoot());
      }
      primaryStage.setTitle(previous.getTitle());
      
      if (previous.getFxmlFile().contains("Login.fxml") || previous.getFxmlFile().contains("SignUp.fxml")) {
        primaryStage.setMaximized(false);
        primaryStage.setResizable(true);
        javafx.application.Platform.runLater(() -> {
          primaryStage.sizeToScene();
          primaryStage.centerOnScreen();
          primaryStage.setResizable(false);
        });
      } else {
        primaryStage.setMaximized(true);
        primaryStage.setResizable(false);
      }
      
      primaryStage.show();

      log.debug("Quay lại màn hình trước đó thành công");
    } else {
      // Fallback về trang chủ nếu không có lịch sử
      switchScreen("Bidder/Home.fxml", "Trang chủ");
    }
  }

  /**
   * Xóa sạch lịch sử các màn hình đã lưu.
   * Thường được sử dụng khi người dùng đăng xuất khỏi hệ thống.
   */
  public static void clearHistory() {
    history.clear();
    currentScreen = null;
  }

  /**
   * Hiển thị một hộp thoại cảnh báo hoặc thông báo cho người dùng.
   *
   * @param type loại cảnh báo (ví dụ: ERROR, WARNING, INFORMATION)
   * @param title tiêu đề của hộp thoại
   * @param content nội dung chi tiết hiển thị trong thông báo
   * @return một Optional chứa ButtonType mà người dùng đã nhấn chọn
   */
  public static Optional<ButtonType> showAlert(Alert.AlertType type, String title, String content) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.initOwner(primaryStage);

    return alert.showAndWait();
  }

  /**
   * Tạo ra cửa sổ con mới, thiết lập quan hệ cha con và căn giữa màn hình.
   * Cửa sổ con sẽ tự động đóng khi cửa sổ cha đóng và ngăn tương tác với cửa sổ cha khi đang mở (modal).
   *
   * @param fxmlFile đường dẫn tệp FXML của màn hình con cần tạo
   * @param title tiêu đề hiển thị của cửa sổ con
   */
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

      // Thiết lập kích thước tối đa dựa trên kích thước của cửa sổ chính
      if (primaryStage != null && primaryStage.getHeight() > 100) {
        newStage.setMaxHeight(Math.max(400, primaryStage.getHeight() - 80));
      }
      if (primaryStage != null && primaryStage.getWidth() > 100) {
        newStage.setMaxWidth(Math.max(600, primaryStage.getWidth() - 80));
      }

      // Tạm ẩn cửa sổ đi (độ mờ = 0) để tránh hiệu ứng "nháy" vị trí
      newStage.setOpacity(0);

      // Lắng nghe sự kiện ngay khi OS vừa cấp phát xong kích thước thực tế
      newStage.setOnShown(event -> {
        double maxHeight = 700;
        double maxWidth = 900;
        if (primaryStage != null && primaryStage.getHeight() > 100) {
          maxHeight = primaryStage.getHeight() - 80;
        }
        if (primaryStage != null && primaryStage.getWidth() > 100) {
          maxWidth = primaryStage.getWidth() - 80;
        }

        if (newStage.getHeight() > maxHeight) {
          newStage.setHeight(maxHeight);
        }
        if (newStage.getWidth() > maxWidth) {
          newStage.setWidth(maxWidth);
        }

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
      log.error("Lỗi hệ thống khi tải cửa sổ con: {}", fxmlFile, e);
      showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình" + fxmlFile);
    }
  }

  /**
   * Tạo ra cửa sổ con mới và trả về bộ điều khiển (Controller) của màn hình đó để xử lý logic.
   *
   * @param <T> kiểu Controller tương ứng của màn hình con
   * @param fxmlFile đường dẫn tệp FXML của màn hình con cần tạo
   * @param title tiêu đề hiển thị của cửa sổ con
   * @return Controller của màn hình con đã được tải, hoặc null nếu xảy ra lỗi
   */
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

      if (primaryStage != null && primaryStage.getHeight() > 100) {
        newStage.setMaxHeight(Math.max(400, primaryStage.getHeight() - 80));
      }
      if (primaryStage != null && primaryStage.getWidth() > 100) {
        newStage.setMaxWidth(Math.max(600, primaryStage.getWidth() - 80));
      }

      newStage.setOpacity(0);

      newStage.setOnShown(event -> {
        double maxHeight = 700;
        double maxWidth = 900;
        if (primaryStage != null && primaryStage.getHeight() > 100) {
          maxHeight = primaryStage.getHeight() - 80;
        }
        if (primaryStage != null && primaryStage.getWidth() > 100) {
          maxWidth = primaryStage.getWidth() - 80;
        }

        if (newStage.getHeight() > maxHeight) {
          newStage.setHeight(maxHeight);
        }
        if (newStage.getWidth() > maxWidth) {
          newStage.setWidth(maxWidth);
        }

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
      log.error("Lỗi hệ thống khi tải cửa sổ con có controller: {}", fxmlFile, e);
      showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình: " + fxmlFile);
      return null;
    }
  }
}