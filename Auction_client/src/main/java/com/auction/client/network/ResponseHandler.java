package com.auction.client.network;

import com.auction.client.screenhandler.HomeController;
import com.auction.client.screenhandler.ScreenController;
import com.auction.client.screenhandler.SellerHomeController;
import com.auction.client.screenhandler.admin.SellerAccountManagerController;
import com.auction.shared.enums.OrderStatus;
import com.auction.shared.enums.SellerRegisterStatus;
import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.GetSellerProfileRequestDTO;
import com.auction.shared.response.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import com.auction.client.screenhandler.ItemAuctionController;

import java.net.URL;
import java.util.ResourceBundle;

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
      UserDTO user = loginRes.getUser();
      SessionManager.setCurrentUser(user);

      if (user.getRole() == UserRole.BIDDER) {
        Platform.runLater(() -> {
          ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
        });
      } else {
        Platform.runLater(() -> {
          ScreenController.switchScreen("Admin/AdminScreen.fxml", "Trang chủ");
        });
      }

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

  public static void handleGetAuctionsBySeller(GetAuctionsBySellerResponseDTO getAuctionsBySellerRes) {
    if (getAuctionsBySellerRes.isSuccess()) {
      Platform.runLater(() -> {
        SellerHomeController sellerHomeController = SellerHomeController.getInstance();
        if (sellerHomeController != null) {
          sellerHomeController.loadFeedToUI(getAuctionsBySellerRes.getActiveAuctions());
        }
      });
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi tải bảng tin", getAuctionsBySellerRes.getMessage());
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
  public static void handlePaymentNotification(PaymentNotificationDTO dto) {
    // hiển thị thông báo thanh toán
    System.out.println("VUI LONG THANH TOAN SAN PHAM: " + dto.getItemName());
    System.out.println("Giá cuối: " + dto.getFinalPrice());
  }

  public static void handleAuctionResult(AuctionResultDTO dto) {
    // Hiển thị lên UI: "Người thắng: winnerId - Giá: finalPrice"
    System.out.println("Phiên " + dto.getAuctionId()
        + " | Người thắng: " + dto.getWinnerId()
        + " | Giá cuối: " + dto.getFinalPrice());
  }

  /**
   * Xử lý gói tin phản hồi khi có người đặt giá mới thành công (Broadcast từ Server).
   *
   * @param newBidDTO Gói tin chứa thông tin giá mới và người đặt
   */
  public static void handleNewBid(NewBidDTO newBidDTO) {
    // Kiểm tra xem người dùng có đang mở màn hình đấu giá không (instance != null)
    if (ItemAuctionController.instance != null) {
      // Đẩy dữ liệu sang Controller để nó tự vẽ lại UI
      ItemAuctionController.instance.onNewBidReceived(newBidDTO);
    }
  }

  // Xử lý khi nhận được kết quả Đặt giá (Thành công/Thất bại do thiếu tiền)
  public static void handlePlaceBidResponse(PlaceBidResponseDTO response) {
    if (com.auction.client.screenhandler.ItemAuctionController.instance != null) {
      com.auction.client.screenhandler.ItemAuctionController.instance.onPlaceBidResponse(response);
    }
  }

  // Xử lý khi nhận được toàn bộ lịch sử đấu giá lúc vừa vào phòng
  public static void handleAuctionRoomJoined(AuctionResponseDTO response) {
    if (com.auction.client.screenhandler.ItemAuctionController.instance != null) {
      com.auction.client.screenhandler.ItemAuctionController.instance.onAuctionRoomJoined(response);
    }
  }

  public static void handleSellerRegister(SellerRegisterResponseDTO sellerRegisterRes) {
    if (sellerRegisterRes.isSuccess()) {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thông báo",
            sellerRegisterRes.getMessage()).ifPresent(Response -> {
           if (Response == ButtonType.OK) {
             ScreenController.switchScreen("Bidder/Home.fxml", "Trang chủ");
           }
        });
      });
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi", sellerRegisterRes.getMessage());
      });
    }
  }

  public static void checkingSellerProfile(CheckingSellerProfileResponseDTO checkingSellerProfileRes) {
    String message = checkingSellerProfileRes.getMessage();
    Platform.runLater(() -> {
      if (SellerRegisterStatus.REGISTERED.toString().equals(message)) {
        ScreenController.switchScreen("Seller/SellerHome.fxml", "Quản lý sản phẩm đăng bán");
      } else if (SellerRegisterStatus.UNREGISTERED.toString().equals(message)) {
        ScreenController.showAlert(Alert.AlertType.INFORMATION,
            "Thông báo", "Hồ sơ của bạn đang được hệ thống phê duyệt. Vui lòng quay lại sau!");
      } else if (SellerRegisterStatus.DENIED.toString().equals(message)) {
        ScreenController.showAlert(Alert.AlertType.WARNING, "Thông báo",
            "Hồ sơ bán hàng của bạn đã bị từ chối!\nBạn không thể sử dụng tính năng này");
      } else {
        ScreenController.showAlert(Alert.AlertType.WARNING, "Thông báo",
            "Bạn cần đăng ký hồ sơ người bán để sử dụng tính năng này.");
        ScreenController.switchScreen("Bidder/SellerRegisterForBidder.fxml", "Đăng ký người bán");
      }
    });
  }
  public static void handleOrderAction(OrderActionResponseDTO dto) {
    Platform.runLater(() -> {
      Alert alert = new Alert(
          dto.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
      );
      alert.setTitle(dto.isSuccess() ? "Thành công" : "Thất bại");
      alert.setHeaderText(dto.getMessage());
      alert.showAndWait();
    });
  }

  public static void handleGetOrder(GetOrderResponseDTO dto) {
    // Ae viet hien thi UI o day
  }

  public static void handleOrderUpdateNotification(OrderUpdateNotificationDTO dto) {
    Platform.runLater(() -> {
      String message = dto.getStatus() == OrderStatus.CONFIRMED
          ? "Người mua đã xác nhận thanh toán đơn hàng!"
          : "Người mua đã hủy đơn hàng!";

      ScreenController.showAlert(
          Alert.AlertType.INFORMATION,
          "Cập nhật đơn hàng",
          message
      );
    });
  }

  public static void handleGetSellerProfile(GetSellerProfileResponseDTO getSellerProfileRes) {
    if (getSellerProfileRes.isSuccess()) {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.INFORMATION,
            "Thông báo", getSellerProfileRes.getMessage());
        SellerAccountManagerController controller = SellerAccountManagerController.getInstance();

        if (controller != null) {
          controller.loadDataToTable(getSellerProfileRes.getSellerProfileList());
        }
      });
    }
  }

  public static void handleUpdateSellerProfileStatus(UpdateSellerProfileStatusResponseDTO updateSellerProfileStatusRes) {
    if (updateSellerProfileStatusRes.isSuccess()) {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.INFORMATION,
            "Thông báo", updateSellerProfileStatusRes.getMessage());
        ServerConnection.sendData(new GetSellerProfileRequestDTO());
      });
    } else  {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR,
            "Lỗi",  updateSellerProfileStatusRes.getMessage());
      });
    }
  }
}
