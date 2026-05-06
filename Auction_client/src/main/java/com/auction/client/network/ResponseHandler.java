package com.auction.client.network;

import com.auction.client.screenhandler.ScreenController;
import com.auction.shared.response.LoginResponseDTO;
import com.auction.shared.response.SignUpResponseDTO;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Lớp xử lý các phản hồi (Response) nhận được từ Server và cập nhật giao diện người dùng (UI).
 * <p>
 * Do JavaFX yêu cầu mọi thay đổi UI (như hiện thông báo, đổi màn hình) phải được
 * thực hiện trên luồng chính (Application Thread), lớp này sử dụng {@code Platform.runLater()}
 * để bọc các thao tác UI một cách an toàn. Nó xử lý kết quả thành công hoặc thất bại
 * dựa trên dữ liệu mang theo trong các {@code ResponseDTO}.
 * </p>
 *
 * @see com.auction.client.screenhandler.ScreenController
 */
public class ResponseHandler {
  // Xử lý phản hồi về yêu cầu đăng nhập của server
  public static void login(LoginResponseDTO loginRes) {
    // Nếu xử lý đăng nhập thành công
    if (loginRes.isSuccess()) {
      Platform.runLater(() -> {
        ScreenController.switchScreen("Home.fxml", "Trang chủ");
      });

      // Nếu xử lý đăng nhập thất bại
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi đăng nhập",  loginRes.getMessage());
      });
    }
  }

  // Xử lý phản hồi về yêu cầu tạo tài khoản của server
  public static void signUp(SignUpResponseDTO signUpRes) {
    // Nếu yêu cầu xử lý thành công
    if (signUpRes.isSuccess()) {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thông báo",
            signUpRes.getMessage()).ifPresent(Response -> {
              if (Response == ButtonType.OK) {
                ScreenController.switchScreen("Login.fxml", "Đăng nhập");
              }
            });
      });

      // Nếu xử lý thất bại
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi đăng kí", signUpRes.getMessage());
      });
    }
  }
}
