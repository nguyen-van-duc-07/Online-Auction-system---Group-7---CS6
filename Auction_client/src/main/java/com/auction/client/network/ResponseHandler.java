package com.auction.client.network;

import com.auction.client.screenhandler.HomeController;
import com.auction.client.screenhandler.ScreenController;
import com.auction.shared.response.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Lớp xử lý các phản hồi (Response) nhận được từ Server và cập nhật giao diện người dùng (UI).
 *
 * <p>Do JavaFX yêu cầu mọi thay đổi UI (như hiện thông báo, đổi màn hình) phải được
 * thực hiện trên luồng chính (Application Thread), lớp này sử dụng {@code Platform.runLater()}
 * để bọc các thao tác UI một cách an toàn. Nó xử lý kết quả thành công hoặc thất bại
 * dựa trên dữ liệu mang theo trong các {@code ResponseDTO}.</p>
 *
 * @see com.auction.client.screenhandler.ScreenController
 */
public class ResponseHandler {
  /**
   * Xử lý gói tin phản hồi đăng nhập từ Server.
   *
   * <p>Lưu thông tin người dùng vào {@link SessionManager} nếu thành công,
   * sau đó chuyển trang.</p>
   *
   * @param loginRes Gói tin nhắn phản hồi đăng nhập
   */
  public static void login(LoginResponseDTO loginRes) {
    // Nếu xử lý đăng nhập thành công
    if (loginRes.isSuccess()) {
      SessionManager.setCurrentUser(loginRes.getUser());
      Platform.runLater(() -> {
        ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
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
                ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");
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

  /**
   * Xử lý gói tin phản hồi đăng bán sản phẩm từ Server.
   *
   * <p>Chuyển hướng về trang chủ của người bán nếu lưu dữ liệu thành công.</p>
   * * @param uploadItemRes Gói tin phản hồi chứa kết quả
   */
  public static void handleUploadItem(UploadItemResponseDTO uploadItemRes) {
    if (uploadItemRes.isSuccess()) {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.INFORMATION,
            "Thông báo", uploadItemRes.getMessage()).ifPresent(Response -> {
              if (Response == ButtonType.OK) {
                ScreenController.switchScreen("Seller/SellerHome.fxml", "Quản lý sản phẩm");
              }
        });
      });
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR,
            "Lỗi", uploadItemRes.getMessage());
      });
    }
  }

  public static void handleGetActiveAuctions(GetActiveAuctionResponseDTO getActiveAuctionRes) {
    if (getActiveAuctionRes.isSuccess()) {
      Platform.runLater(() -> {
        // Lấy controller của trang Home hiện tại đang mở trên màn hình
        HomeController homeController = HomeController.getInstance();

        if (homeController != null) {
          // Gọi hàm vẽ UI và truyền danh sách sản phẩm vào
          homeController.loadFeedToUI(getActiveAuctionRes.getActiveAuctions());
        }
      });
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR,
            "Lỗi tải bảng tin", getActiveAuctionRes.getMessage());
      });
    }
  }

  public static void handleUpdateProfile(UpdateProfileResponseDTO updateProfileRes) {
    if (updateProfileRes.isSuccess()) {
      SessionManager.setCurrentUser(updateProfileRes.getUserAfterUpdatingProfile());
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.INFORMATION,
            "Thông báo", updateProfileRes.getMessage()).ifPresent(Response -> {
              if (Response == ButtonType.OK) {
                ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
              }
        });
      });
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR,
            "Lỗi cập nhật", updateProfileRes.getMessage());
      });
    }
  }
}
