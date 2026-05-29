package com.auction.client.network;

import com.auction.client.screenhandler.*;
import com.auction.client.screenhandler.admin.AdminScreenController;
import com.auction.client.screenhandler.admin.AuctionManagerController;
import com.auction.client.screenhandler.admin.PendingTransactionManagerController;
import com.auction.client.screenhandler.admin.SellerAccountManagerController;
import com.auction.shared.util.CurrencyUtils;
import com.auction.shared.enums.*;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.*;
import com.auction.shared.response.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lớp xử lý các phản hồi (Response) nhận được từ Server và cập nhật giao diện người dùng (UI).
 *
 * <p>Do JavaFX yêu cầu mọi thay đổi UI (như hiện thông báo, đổi màn hình) phải được
 * thực hiện trên luồng chính (Application Thread), lớp này sử dụng {@code Platform.runLater()}
 * để bọc các thao tác UI một cách an sau. Nó xử lý kết quả thành công hoặc thất bại
 * dựa trên dữ liệu mang theo trong các {@code ResponseDTO}.</p>
 *
 * @see com.auction.client.screenhandler.ScreenController
 */
public class ResponseHandler {
  private static final Logger log = LoggerFactory.getLogger(ResponseHandler.class);

  /**
   * Xử lý gói tin phản hồi đăng nhập từ Server.
   *
   * <p>Lưu thông tin người dùng vào {@link SessionManager} nếu thành công,
   * sau đó chuyển trang.</p>
   *
   * @param loginRes Gói tin nhắn phản hồi đăng nhập
   */
  public static void login(LoginResponseDTO loginRes) {
    if (loginRes.isSuccess()) {
      UserDTO user = loginRes.getUser();
      SessionManager.setCurrentUser(user);

      if (user.getRole() == UserRole.BIDDER) {
        // Bắn Request xin số dư ngay khi lưu user thành công
        ServerConnection.sendData(new GetBalanceRequestDTO());
        Platform.runLater(() -> {
          ScreenController.switchScreen("MainLayout.fxml", "Trang chủ");
        });
      } else {
        Platform.runLater(() -> {
          ScreenController.switchScreen("Admin/AdminScreen.fxml", "Trang chủ");
        });
      }

    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi đăng nhập", loginRes.getMessage());
      });
    }
  }

  /**
   * Xử lý gói tin phản hồi đăng ký tài khoản từ Server.
   *
   * @param signUpRes Gói tin phản hồi chứa kết quả đăng ký
   */
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
   * <p>
   * Chuyển hướng về trang chủ của người bán nếu lưu dữ liệu thành công.
   * </p>
   *
   * @param uploadItemRes Gói tin phản hồi chứa kết quả đăng bán sản phẩm
   */
  public static void handleUploadItem(UploadItemResponseDTO uploadItemRes) {
    if (uploadItemRes.isSuccess()) {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.INFORMATION,
            "Thông báo", uploadItemRes.getMessage()).ifPresent(Response -> {
          if (Response == ButtonType.OK) {
            MainLayoutController controller = MainLayoutController.getInstance();
            if (controller != null) {
              controller.showSellerHome();
            }
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

  /**
   * Xử lý gói tin phản hồi tìm kiếm đấu giá theo Id từ Server.
   *
   * @param response Gói tin phản hồi chứa chi tiết đấu giá và sản phẩm tương ứng
   */
  public static void handleFindAuctionById(AuctionResponseDTO response) {
    if (response != null) {
      Platform.runLater(() -> {
        String title = "Chi tiết sản phẩm " + response.getItem().getName();
        ItemViewController itemViewController = ScreenController.createSubWindowAndGetController("Seller/ItemView.fxml", title);
        itemViewController.initData(response);
      });
    }
  }

  /**
   * Xử lý phản hồi lấy danh sách các phiên đấu giá đang diễn ra.
   *
   * @param getActiveAuctionRes Gói tin phản hồi chứa danh sách đấu giá đang diễn ra
   */
  public static void handleGetActiveAuctions(GetActiveAuctionsResponseDTO getActiveAuctionRes) {
    if (getActiveAuctionRes.isSuccess()) {
      Platform.runLater(() -> {
        MainLayoutController controller = MainLayoutController.getInstance();
        if (controller != null && controller.getHomeController() != null) {
          controller.getHomeController().loadFeedToUI(getActiveAuctionRes.getActiveAuctions());
        }
      });
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR,
            "Lỗi tải bảng tin", getActiveAuctionRes.getMessage());
      });
    }
  }

  /**
   * Xử lý phản hồi lấy danh sách các phiên đấu giá sắp diễn ra.
   *
   * @param getWaitingAuctionsRes Gói tin phản hồi chứa danh sách đấu giá sắp diễn ra
   */
  public static void handleGetWaitingAuctions(GetWaitingAuctionsResponseDTO getWaitingAuctionsRes) {
    if (getWaitingAuctionsRes.isSuccess()) {
      Platform.runLater(() -> {
        MainLayoutController controller = MainLayoutController.getInstance();
        if (controller != null && controller.getHomeController() != null) {
          controller.getHomeController().loadFeedToUI(getWaitingAuctionsRes.getWaitingAuctions());
        }
      });
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR,
            "Lỗi tải bảng tin", getWaitingAuctionsRes.getMessage());
      });
    }
  }

  /**
   * Xử lý phản hồi lấy danh sách các phiên đấu giá đã kết thúc.
   *
   * @param getClosedAuctionsRes Gói tin phản hồi chứa danh sách đấu giá đã kết thúc
   */
  public static void handleGetClosedAuctions(GetClosedAuctionsResponseDTO getClosedAuctionsRes) {
    if (getClosedAuctionsRes.isSuccess()) {
      Platform.runLater(() -> {
        MainLayoutController controller = MainLayoutController.getInstance();
        if (controller != null && controller.getHomeController() != null) {
          controller.getHomeController().loadFeedToUI(getClosedAuctionsRes.getClosedAuctions());
        }
      });
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR,
            "Lỗi tải bảng tin", getClosedAuctionsRes.getMessage());
      });
    }
  }

  /**
   * Xử lý phản hồi lấy danh sách đấu giá đang hoạt động của người bán.
   *
   * @param response Gói tin phản hồi chứa danh sách đấu giá đang hoạt động của người bán
   */
  public static void handleGetActiveAuctionsBySelelr(GetActiveAuctionsBySellerResponseDTO response) {
    if (response.isSuccess()) {
      Platform.runLater(() -> {
        MainLayoutController controller = MainLayoutController.getInstance();
        if (controller != null && controller.getSellerHomeController() != null) {
          controller.getSellerHomeController().loadSellerFeedToUI(response.getActiveAuctionsBelongToSeller());
        }
      });
    }
  }

  /**
   * Xử lý phản hồi lấy toàn bộ danh sách phiên đấu giá của người bán.
   *
   * @param getAuctionsBySellerRes Gói tin phản hồi chứa danh sách toàn bộ đấu giá của người bán
   */
  public static void handleGetAuctionsBySeller(GetAuctionsBySellerResponseDTO getAuctionsBySellerRes) {
    if (getAuctionsBySellerRes.isSuccess()) {
      Platform.runLater(() -> {
        MainLayoutController controller = MainLayoutController.getInstance();
        if (controller != null && controller.getSellerHomeController() != null) {
          controller.getSellerHomeController().loadSellerFeedToUI(getAuctionsBySellerRes.getActiveAuctions());
        }
      });
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi tải bảng tin", getAuctionsBySellerRes.getMessage());
      });
    }
  }

  /**
   * Xử lý gói tin phản hồi cập nhật thông tin cá nhân từ Server.
   *
   * @param updateProfileRes Gói tin phản hồi chứa thông tin cá nhân sau cập nhật
   */
  public static void handleUpdateProfile(UpdateProfileResponseDTO updateProfileRes) {
    if (updateProfileRes.isSuccess()) {
      UserDTO updatedUser = updateProfileRes.getUserAfterUpdatingProfile();
      SessionManager.setCurrentUser(updatedUser);
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.INFORMATION,
            "Thông báo", updateProfileRes.getMessage()).ifPresent(Response -> {
          if (Response == ButtonType.OK) {
            SessionManager.setCurrentUser(updatedUser);
            if (updatedUser.getRole() == UserRole.ADMIN) {
              AdminScreenController adminController = AdminScreenController.getInstance();
              if (adminController != null) {
                adminController.updateAdminName(updatedUser.getAccountName());
                adminController.gotoProfile();
              }
            } else {
              MainLayoutController controller = MainLayoutController.getInstance();
              if (controller != null) {
                Label accountNameLabel = controller.getAccountNameLabel();
                if (accountNameLabel != null) {
                  accountNameLabel.setText("Chào, " + updatedUser.getAccountName());
                }
                controller.gotoHomeFeed();
              }
            }
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

  /**
   * Xử lý gói tin phản hồi thay đổi mật khẩu từ Server.
   *
   * @param response Gói tin phản hồi chứa kết quả thay đổi mật khẩu
   */
  public static void handleChangePassword(ChangePasswordResponseDTO response) {
    Platform.runLater(() -> {
      if (response.isSuccess()) {
        ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thành công", response.getMessage());
        if (ChangePasswordController.getInstance() != null) {
          ChangePasswordController.getInstance().closeWindow();
        }
      } else {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Thất bại", response.getMessage());
      }
    });
  }

  /**
   * Xử lý thông báo thắng thầu và yêu cầu thanh toán đơn hàng.
   *
   * @param dto Gói tin chứa thông tin đấu giá đã thắng và mã đơn hàng tương ứng
   */
  public static void handlePaymentNotification(PaymentNotificationDTO dto) {
    Platform.runLater(() -> {
      ToastNotification.show(
          ScreenController.primaryStage,
          "Chúc mừng! Bạn đã thắng!",
          dto.getItemName() + "\n" + CurrencyUtils.formatVnd(dto.getFinalPrice()) + " • Nhấn để thanh toán",
          () -> ServerConnection.sendData(new GetOrderRequestDTO(dto.getOrderId()))
      );
    });

    log.info("VUI LÒNG THANH TOÁN SẢN PHẨM: {} | Giá cuối: {}", dto.getItemName(), dto.getFinalPrice());
  }

  /**
   * Xử lý gói tin thông báo kết quả chung cuộc của phiên đấu giá.
   *
   * @param dto Gói tin chứa thông tin người thắng thầu và mức giá thắng thầu
   */
  public static void handleAuctionResult(AuctionResultDTO dto) {
    // Hiển thị lên UI: "Người thắng: winnerId - Giá: finalPrice"
    log.info("Phiên {} | Người thắng: {} | Giá cuối: {}",
        dto.getAuctionId(), dto.getWinnerId(), dto.getFinalPrice());
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

  /**
   * Xử lý kết quả phản hồi khi người dùng đặt giá thầu (thành công hoặc thất bại).
   *
   * @param response Gói tin phản hồi chứa kết quả đặt giá thầu
   */
  public static void handlePlaceBidResponse(PlaceBidResponseDTO response) {
    if (com.auction.client.screenhandler.ItemAuctionController.instance != null) {
      com.auction.client.screenhandler.ItemAuctionController.instance.onPlaceBidResponse(response);
    }
    if (response.isSuccess()) {
      ServerConnection.sendData(new GetBalanceRequestDTO());
    }
  }

  /**
   * Xử lý phản hồi khi người dùng tham gia phòng đấu giá thành công.
   *
   * @param response Gói tin phản hồi chứa chi tiết phòng đấu giá và cấu hình tự động nếu có
   */
  public static void handleAuctionRoomJoined(JoinRoomResponseDTO response) {
    if (response.isSuccess()) {
      if (ItemAuctionController.instance != null) {
        ItemAuctionController.instance.onAuctionRoomJoined(response.getAuction(), response.getAutoBidConfig());
      }
    }
  }

  /**
   * Xử lý phản hồi đăng ký thông tin người bán từ Server.
   *
   * @param sellerRegisterRes Gói tin phản hồi chứa kết quả đăng ký
   */
  public static void handleSellerRegister(SellerRegisterResponseDTO sellerRegisterRes) {
    if (sellerRegisterRes.isSuccess()) {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thông báo",
            sellerRegisterRes.getMessage()).ifPresent(Response -> {
          if (Response == ButtonType.OK) {
            MainLayoutController controller = MainLayoutController.getInstance();
            if (controller != null) {
              controller.gotoHomeFeed();
            }
          }
        });
      });
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi", sellerRegisterRes.getMessage());
      });
    }
  }

  /**
   * Kiểm tra hồ sơ/trạng thái người bán để điều hướng màn hình phù hợp.
   *
   * @param checkingSellerProfileRes Gói tin chứa thông tin trạng thái phê duyệt hồ sơ người bán
   */
  public static void checkingSellerProfile(CheckingSellerProfileResponseDTO checkingSellerProfileRes) {
    MainLayoutController controller = MainLayoutController.getInstance();
    String message = checkingSellerProfileRes.getMessage();
    Platform.runLater(() -> {
      if (SellerRegisterStatus.REGISTERED.toString().equals(message)) {
        if (controller != null) {
          controller.showSellerHome();
        }
      } else if (SellerRegisterStatus.UNREGISTERED.toString().equals(message)) {
        ScreenController.showAlert(Alert.AlertType.INFORMATION,
            "Thông báo", "Hồ sơ của bạn đang được hệ thống phê duyệt. Vui lòng quay lại sau!");
      } else if (SellerRegisterStatus.DENIED.toString().equals(message)) {
        ScreenController.showAlert(Alert.AlertType.WARNING, "Thông báo",
            "Hồ sơ bán hàng của bạn đã bị từ chối!\nBạn không thể sử dụng tính năng này");
      } else {
        ScreenController.showAlert(Alert.AlertType.WARNING, "Thông báo",
            "Bạn cần đăng ký hồ sơ người bán để sử dụng tính năng này.").ifPresent(Response -> {
          if (Response == ButtonType.OK) {
            if (controller != null) {
              controller.loadComponent("/com/auction/client/Bidder/SellerRegisterForBidder.fxml");
            }
          }
        });
      }
    });
  }

  /**
   * Xử lý kết quả phản hồi khi thực hiện hành động với đơn hàng (thanh toán/hủy).
   *
   * @param dto Gói tin phản hồi chứa kết quả thực hiện hành động đơn hàng
   */
  public static void handleOrderAction(OrderActionResponseDTO dto) {
    Platform.runLater(() -> {
      if (PaymentScreenController.instance != null) {
        if (dto.isSuccess()) {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("Thành công");
          alert.setHeaderText(dto.getMessage());
          alert.showAndWait();
          PaymentScreenController.instance.onOrderActionSuccess(dto.getMessage());
        } else {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Thất bại");
          alert.setHeaderText(dto.getMessage());
          alert.showAndWait();
          PaymentScreenController.instance.onOrderActionFailed(dto.getMessage());
        }
      } else {
        Alert alert = new Alert(
            dto.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
        );
        alert.setTitle(dto.isSuccess() ? "Thành công" : "Thất bại");
        alert.setHeaderText(dto.getMessage());
        alert.showAndWait();
      }
    });
  }

  /**
   * Xử lý phản hồi lấy thông tin chi tiết đơn hàng để hiển thị màn hình thanh toán.
   *
   * @param dto Gói tin chứa thông tin đơn hàng chi tiết
   */
  public static void handleGetOrder(GetOrderResponseDTO dto) {
    if (dto.isSuccess()) {
      Platform.runLater(() -> {
        SessionManager.setCurrentOrderId(dto.getOrder().getId());
        ScreenController.switchScreen("Bidder/PaymentScreen.fxml", "Chi tiết đơn hàng");
        if (PaymentScreenController.instance != null) {
          PaymentScreenController.instance.setOrderData(dto.getOrder());
        }
      });
    } else {
      Platform.runLater(() -> {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi", dto.getMessage());
      });
    }
  }

  /**
   * Xử lý thông báo cập nhật trạng thái đơn hàng (được xác nhận hoặc bị hủy).
   *
   * @param dto Gói tin chứa thông tin trạng thái cập nhật đơn hàng
   */
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

  /**
   * Xử lý phản hồi lấy danh sách hồ sơ đăng ký của người bán dành cho quản trị viên.
   *
   * @param getSellerProfileRes Gói tin chứa danh sách hồ sơ đăng ký người bán
   */
  public static void handleGetSellerProfile(GetSellerProfileResponseDTO getSellerProfileRes) {
    if (getSellerProfileRes.isSuccess()) {
      Platform.runLater(() -> {
        SellerAccountManagerController controller = SellerAccountManagerController.getInstance();

        if (controller != null) {
          controller.loadDataToTable(getSellerProfileRes.getSellerProfileList());
        }
      });
    }
  }

  /**
   * Xử lý phản hồi lấy toàn bộ danh sách người dùng trong hệ thống dành cho Admin.
   *
   * @param response Gói tin phản hồi chứa danh sách tất cả người dùng
   */
  public static void handleGetAllUsers(com.auction.shared.response.GetAllUsersResponseDTO response) {
    if (response.isSuccess()) {
      Platform.runLater(() -> {
        com.auction.client.screenhandler.admin.UserManagerController controller = com.auction.client.screenhandler.admin.UserManagerController.getInstance();
        if (controller != null) {
          controller.loadDataToTable(response.getUsers());
        }
      });
    }
  }

  /**
   * Xử lý phản hồi lấy danh sách các phiên đấu giá đang diễn ra và sắp diễn ra.
   *
   * @param getActiveAndWaitingAuctionsRes Gói tin chứa danh sách các đấu giá hoạt động và chờ
   */
  public static void handleGetActiveAndWaitingAuctions(
      GetActiveAndWaitingAuctionsResponseDTO getActiveAndWaitingAuctionsRes) {
    if (getActiveAndWaitingAuctionsRes.isSuccess()) {
      Platform.runLater(() -> {
        if (SessionManager.getCurrentUser() == null) {
          return;
        }
        // Lấy role của user hiện tại từ SessionManager
        UserRole currentRole = SessionManager.getCurrentUser().getRole();

        if (currentRole == UserRole.ADMIN) {
          // NẾU LÀ ADMIN -> Đẩy dữ liệu vào bảng quản lý của Admin
          AuctionManagerController controller = AuctionManagerController.getInstance();
          if (controller != null) {
            controller.loadDataToTable(getActiveAndWaitingAuctionsRes.getActiveAndWaitingAuctions());
          }
        } else {
          // NẾU LÀ USER (BIDDER/SELLER) -> Xử lý hiển thị cho User
          MainLayoutController controller = MainLayoutController.getInstance();
          if (controller != null && controller.getHomeController() != null) {
            controller.getHomeController().loadFeedToUI(getActiveAndWaitingAuctionsRes.getActiveAndWaitingAuctions());
          }
        }
      });
    }
  }

  /**
   * Xử lý phản hồi lấy danh sách các phiên đấu giá đã bị hủy dành cho Admin.
   *
   * @param getCanceledAuctionsRes Gói tin phản hồi chứa danh sách đấu giá bị hủy
   */
  public static void handleGetCanceledAuctions(
      GetCanceledAuctionsResponseDTO getCanceledAuctionsRes) {
    if (getCanceledAuctionsRes.isSuccess()) {
      Platform.runLater(() -> {
        AuctionManagerController controller = AuctionManagerController.getInstance();
        if (controller != null) {
          controller.loadDataToTable(getCanceledAuctionsRes.getCanceledAuctions());
        }
      });
    }
  }

  /**
   * Xử lý phản hồi cập nhật trạng thái phê duyệt hồ sơ người bán dành cho Admin.
   *
   * @param updateSellerProfileStatusRes Gói tin chứa kết quả cập nhật trạng thái hồ sơ người bán
   */
  public static void handleUpdateSellerProfileStatus(UpdateSellerProfileStatusResponseDTO updateSellerProfileStatusRes) {
    Platform.runLater(() -> {
      if (updateSellerProfileStatusRes.isSuccess()) {
        ScreenController.showAlert(Alert.AlertType.INFORMATION,
            "Thông báo", updateSellerProfileStatusRes.getMessage());
      } else {
        ScreenController.showAlert(Alert.AlertType.ERROR,
            "Lỗi", updateSellerProfileStatusRes.getMessage());
      }
      // Luôn tải lại danh sách để đồng bộ trạng thái mới nhất hiển thị trên bảng
      ServerConnection.sendData(new GetSellerProfileRequestDTO());
    });
  }

  /**
   * Xử lý phản hồi khi yêu cầu hủy các đấu giá của người bán.
   *
   * @param response Gói tin phản hồi chứa kết quả hủy đấu giá của người bán
   */
  public static void handleCancelSellerAuctions(CancelSellerAuctionsResponseDTO response) {
    Platform.runLater(() -> {
      ScreenController.showAlert(
          response.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
          response.isSuccess() ? "Thông báo" : "Lỗi",
          response.getMessage()
      );
    });
  }

  /**
   * Xử lý phản hồi khi yêu cầu khôi phục các đấu giá của người bán.
   *
   * @param response Gói tin phản hồi chứa kết quả khôi phục đấu giá của người bán
   */
  public static void handleRestoreSellerAuctions(RestoreSellerAuctionsResponseDTO response) {
    Platform.runLater(() -> {
      ScreenController.showAlert(
          response.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
          response.isSuccess() ? "Thông báo" : "Lỗi",
          response.getMessage()
      );
    });
  }

  /**
   * Xử lý gói tin cập nhật giá mới của phiên đấu giá trên giao diện người dùng.
   *
   * @param dto Gói tin chứa mã đấu giá và mức giá mới được cập nhật
   */
  public static void handleAuctionPriceUpdate(AuctionPriceUpdateDTO dto) {
    Platform.runLater(() -> {
      MainLayoutController controller = MainLayoutController.getInstance();
      if (controller != null) {
        if ("home".equals(controller.getCurrentContext()) && controller.getHomeController() != null) {
          controller.getHomeController().updateAuctionPrice(dto.getAuctionId(), dto.getNewPrice());
        } else if ("seller".equals(controller.getCurrentContext()) && controller.getSellerHomeController() != null) {
          controller.getSellerHomeController().updateAuctionPrice(dto.getAuctionId(), dto.getNewPrice());
        }
      }
    });
  }

  /**
   * Xử lý phản hồi cấu hình đặt giá tự động (Auto Bid) từ Server.
   *
   * @param dto Gói tin phản hồi chứa kết quả cấu hình đặt giá tự động
   */
  public static void handleAutoBidResponse(AutoBidResponseDTO dto) {
    Platform.runLater(() -> {
      ScreenController.showAlert(
          dto.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
          dto.isSuccess() ? "Thành công" : "Thất bại",
          dto.getMessage()
      );
    });
  }

  /**
   * Xử lý phản hồi lấy số dư ví của người dùng.
   *
   * @param balanceRes Gói tin phản hồi chứa thông tin số dư
   */
  public static void handleGetBalance(GetBalanceResponseDTO balanceRes) {
    if (balanceRes.isSuccess()) {
      log.info("Lấy số dư thành công: {}", balanceRes.getBalance());
      SessionManager.updateBalance(balanceRes);
      // Gọi sang Controller giao diện để cập nhật Label
      if (WalletController.getInstance() != null) {
        WalletController.getInstance().updateBalanceUI(balanceRes.getBalance());
      }
    } else {
      log.error("Lỗi lấy số dư: {}", balanceRes.getMessage());
      if (WalletController.getInstance() != null) {
        WalletController.getInstance().showErrorUI();
      }
    }
  }

  /**
   * Xử lý phản hồi thanh toán hóa đơn đơn hàng của người mua.
   *
   * @param responseDTO Gói tin phản hồi chứa kết quả thanh toán đơn hàng
   */
  public static void handlePaymentResponse(PaymentResponseDTO responseDTO) {
    // Kiểm tra xem màn hình thanh toán có đang mở không
    if (PaymentScreenController.instance != null) {

      // Đẩy dữ liệu ngược về màn hình Controller để nó cập nhật giao diện
      PaymentScreenController.instance.processPaymentResponse(
          responseDTO.isSuccess(),
          responseDTO.getMessage(),
          responseDTO.getTransaction()
      );
    }
    if (responseDTO.isSuccess()) {
      ServerConnection.sendData(new GetBalanceRequestDTO());
    }
  }

  /**
   * Xử lý phản hồi lấy danh sách các thông báo của người dùng.
   *
   * @param dto Gói tin phản hồi chứa danh sách thông báo và số lượng thông báo chưa đọc
   */
  public static void handleGetNotifications(GetNotificationsResponseDTO dto) {
    Platform.runLater(() -> {
      // Cập nhật badge trên MainLayout
      MainLayoutController controller = MainLayoutController.getInstance();
      if (controller != null) {
        controller.updateNotificationBadge(dto.getUnreadCount());
      }

      // Load danh sách nếu đang ở màn hình thông báo
      if (NotificationController.instance != null) {
        NotificationController.instance.loadNotifications(
            dto.getNotifications(),
            dto.getUnreadCount()
        );
      }
    });
  }

  /**
   * Xử lý khi nhận được một thông báo mới thời gian thực từ Server.
   *
   * @param dto Gói tin chứa nội dung thông báo mới
   */
  public static void handleNewNotification(NotificationDTO dto) {
    Platform.runLater(() -> {
      // Cập nhật badge
      MainLayoutController controller = MainLayoutController.getInstance();
      if (controller != null) {
        controller.incrementNotificationBadge();
      }
      log.info("[CLIENT] Thông báo mới: {}", dto.getTitle());

      if (dto.getType() == NotificationType.SYSTEM && dto.getReferenceId() != null) {
        ToastNotification.show(
            ScreenController.primaryStage,
            dto.getTitle(),
            dto.getContent() + "\n• Nhấn để tham gia ngay!",
            () -> {
              SessionManager.setCurrentAuctionId(dto.getReferenceId());
              ServerConnection.sendData(new JoinRoomRequestDTO(dto.getReferenceId()));
              Platform.runLater(() -> {
                ScreenController.switchScreen("Bidder/ItemAuction.fxml", "Phòng đấu giá");
              });
            }
        );
      }
    });
    if (SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getRole() == UserRole.BIDDER) {
      ServerConnection.sendData(new GetBalanceRequestDTO());
    }
  }

  /**
   * Xử lý khi cấu hình đặt giá tự động (Auto Bid) bị đè/bị đánh bại bởi giá thầu khác.
   *
   * @param dto Gói tin chứa thông tin đấu giá và thông báo lỗi tương ứng
   */
  public static void handleAutoBidDefeated(AutoBidDefeatedDTO dto) {
    // Đảm bảo chỉ giật giao diện nếu người dùng đang ở đúng phòng đấu giá đó
    if (SessionManager.getCurrentAuctionId() != null &&
        SessionManager.getCurrentAuctionId().equals(dto.getAuctionId())) {

      if (ItemAuctionController.instance != null) {
        // Gọi hàm điều khiển HMI (tắt check box, hiện cảnh báo đỏ) mà chúng ta đã viết lúc nãy
        ItemAuctionController.instance.onAutoBidDefeated(dto.getMessage());
      }
    }
  }

  /**
   * Xử lý phản hồi lấy danh sách các đơn hàng đang chờ xử lý của người bán.
   *
   * @param response Gói tin phản hồi chứa danh sách đơn hàng chờ xử lý
   */
  public static void handleGetPendingOrdersOfSeller(GetPendingOrdersOfSellerResponseDTO response) {
    Platform.runLater(() -> {
      if (response.isSuccess()) {
        MainLayoutController controller = MainLayoutController.getInstance();
        if (controller != null && controller.getResultController() != null) {
          controller.getResultController().loadOrdersToUI(response.getPendingOrders());
        }
      }
    });
  }

  /**
   * Xử lý phản hồi lấy danh sách các đơn hàng đang chờ xử lý của người mua.
   *
   * @param response Gói tin phản hồi chứa danh sách đơn hàng chờ xử lý
   */
  public static void handleGetPendingOrdersOfBuyer(GetPendingOrdersOfBuyerResponseDTO response) {
    Platform.runLater(() -> {
      if (response.isSuccess()) {
        MainLayoutController controller = MainLayoutController.getInstance();
        if (controller != null && controller.getResultController() != null) {
          controller.getResultController().loadOrdersToUI(response.getPendingOrders());
        }
      }
    });
  }

  /**
   * Xử lý phản hồi lấy danh sách các đơn hàng đã hoàn thành của người mua.
   *
   * @param response Gói tin phản hồi chứa danh sách đơn hàng hoàn thành
   */
  public static void handleGetCompletedOrdersOfBuyer(GetCompletedOrdersOfBuyerResponseDTO response) {
    Platform.runLater(() -> {
      if (response.isSuccess()) {
        MainLayoutController controller = MainLayoutController.getInstance();
        if (controller != null && controller.getResultController() != null) {
          controller.getResultController().loadOrdersToUI(response.getCompletedOrders());
        }
      }
    });
  }

  /**
   * Xử lý phản hồi lấy danh sách các đơn hàng đã hủy của người mua.
   *
   * @param response Gói tin phản hồi chứa danh sách đơn hàng đã hủy
   */
  public static void handleGetCancelledOrdersOfBuyer(GetCancelledOrdersOfBuyerResponseDTO response) {
    Platform.runLater(() -> {
      if (response.isSuccess()) {
        MainLayoutController controller = MainLayoutController.getInstance();
        if (controller != null && controller.getResultController() != null) {
          controller.getResultController().loadOrdersToUI(response.getCancelledOrders());
        }
      }
    });
  }

  /**
   * Xử lý phản hồi lấy danh sách các đơn hàng đã hoàn thành của người bán.
   *
   * @param response Gói tin phản hồi chứa danh sách đơn hàng hoàn thành
   */
  public static void handleGetCompletedOrdersOfSeller(GetCompletedOrdersOfSellerResponseDTO response) {
    Platform.runLater(() -> {
      if (response.isSuccess()) {
        MainLayoutController controller = MainLayoutController.getInstance();
        if (controller != null && controller.getResultController() != null) {
          controller.getResultController().loadOrdersToUI(response.getCompletedOrders());
        }
      }
    });
  }

  /**
   * Xử lý phản hồi lấy danh sách các đơn hàng đã hủy của người bán.
   *
   * @param response Gói tin phản hồi chứa danh sách đơn hàng đã hủy
   */
  public static void handleGetCancelledOrdersOfSeller(GetCancelledOrdersOfSellerResponseDTO response) {
    Platform.runLater(() -> {
      if (response.isSuccess()) {
        MainLayoutController controller = MainLayoutController.getInstance();
        if (controller != null && controller.getResultController() != null) {
          controller.getResultController().loadOrdersToUI(response.getCancelledOrders());
        }
      }
    });
  }

  /**
   * Xử lý thông báo khi một phiên đấu giá được gia hạn thêm thời gian.
   *
   * @param dto Gói tin chứa thông tin thời gian kết thúc mới của đấu giá
   */
  public static void handleAuctionExtended(AuctionExtendedDTO dto) {
    Platform.runLater(() -> {
      // Cập nhật endTime trong ItemAuctionController
      if (ItemAuctionController.instance != null) {
        ItemAuctionController.instance.onAuctionExtended(dto.getNewEndTime());
      }
      HomeController home = HomeController.getInstance();
      if (home != null) {
        home.updateTimeExtend(dto.getAuctionId(), dto.getNewEndTime());
      }
    });
  }

  /**
   * Xử lý phản hồi từ việc gửi yêu cầu giao dịch (ví dụ: nạp/rút tiền).
   *
   * @param response Gói tin phản hồi chứa kết quả tạo giao dịch
   */
  public static void handleCreateTransactionResponse(CreateTransactionResponseDTO response) {
    Platform.runLater(() -> {
      ScreenController.showAlert(
          response.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
          response.isSuccess() ? "Thành công" : "Lỗi",
          response.getMessage()
      );
      if (response.isSuccess()) {
        ServerConnection.sendData(new GetBalanceRequestDTO());
      }
    });
  }

  /**
   * Xử lý phản hồi lấy danh sách các giao dịch đang chờ xử lý dành cho Admin.
   *
   * @param response Gói tin phản hồi chứa danh sách giao dịch chờ phê duyệt
   */
  public static void handleGetPendingTransactionsResponse(GetPendingTransactionsResponseDTO response) {
    Platform.runLater(() -> {
      if (response.isSuccess()) {
        PendingTransactionManagerController controller = PendingTransactionManagerController.getInstance();
        if (controller != null) {
          controller.loadDataToTable(response.getPendingTransactions());
        }
      } else {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi", response.getMessage());
      }
    });
  }

  /**
   * Xử lý phản hồi phê duyệt/từ chối giao dịch tài chính từ Server dành cho Admin.
   *
   * @param response Gói tin phản hồi chứa kết quả xử lý giao dịch tài chính
   */
  public static void handleProcessTransactionResponse(ProcessTransactionResponseDTO response) {
    Platform.runLater(() -> {
      ScreenController.showAlert(
          response.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
          response.isSuccess() ? "Thành công" : "Lỗi",
          response.getMessage()
      );
      // Reload danh sách pending
      if (response.isSuccess()) {
        ServerConnection.sendData(new GetPendingTransactionsRequestDTO());
      }
    });
  }

  /**
   * Xử lý phản hồi khi cập nhật trạng thái đấu giá (phê duyệt/hủy/mở) dành cho Admin.
   *
   * @param response Gói tin phản hồi chứa kết quả cập nhật trạng thái đấu giá
   */
  public static void handleUpdateAuctionStatus(UpdateAuctionStatusResponseDTO response) {
    Platform.runLater(() -> {
      if (response.isSuccess()) {
        ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thành công", response.getMessage());
        ServerConnection.sendData(new com.auction.shared.request.GetActiveAndWaitingAuctionsRequestDTO());
      } else {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Thất bại", response.getMessage());
      }
    });
  }

  /**
   * Xử lý phản hồi khi tạo tài khoản Admin mới từ Server.
   *
   * @param response Gói tin phản hồi chứa kết quả tạo tài khoản Admin
   */
  public static void handleCreateAdmin(CreateAdminResponseDTO response) {
    Platform.runLater(() -> {
      if (response.isSuccess()) {
        ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thành công", response.getMessage());
        com.auction.client.screenhandler.admin.UserManagerController controller = com.auction.client.screenhandler.admin.UserManagerController.getInstance();
        if (controller != null) {
          controller.hideCreateAdminSection();
        }
        ServerConnection.sendData(new com.auction.shared.request.GetAllUsersRequestDTO());
      } else {
        ScreenController.showAlert(Alert.AlertType.ERROR, "Thất bại", response.getMessage());
      }
    });
  }

  /**
   * Xử lý thông báo thay đổi trạng thái phiên đấu giá từ Server (ví dụ: bị đóng hoặc hủy bởi Admin).
   *
   * @param dto Gói tin chứa thông tin mã phiên và trạng thái mới
   */
  public static void handleAuctionStatusUpdate(AuctionStatusUpdateDTO dto) {
    Platform.runLater(() -> {
      if (SessionManager.getCurrentUser() == null) {
        return;
      }

      String auctionId = dto.getId();
      AuctionStatus newStatus = dto.getAuctionStatus();

      // 1. Kiểm tra nếu người dùng hiện tại đang ở đúng phòng đấu giá bị đóng/hủy này
      if (SessionManager.getCurrentAuctionId() != null && SessionManager.getCurrentAuctionId().equals(auctionId)) {
        if (newStatus == AuctionStatus.CLOSED || newStatus == AuctionStatus.CANCELED) {
          if (ItemAuctionController.instance != null) {
            ItemAuctionController.instance.stopCountdownTimer();
          }
          SessionManager.setCurrentAuctionId(null);

          String alertMsg = (newStatus == AuctionStatus.CLOSED)
              ? "Phiên đấu giá này đã được đóng bởi quản trị viên!"
              : "Phiên đấu giá này đã bị hủy/chặn bởi quản trị viên!";

          ScreenController.showAlert(Alert.AlertType.WARNING, "Thông báo từ hệ thống", alertMsg);

          // Trở lại màn hình trước đó (MainLayout) bằng cách pop lịch sử
          ScreenController.goBack();
        }
      }

      // 2. Cập nhật danh sách trang chủ/trang bán hàng nếu người dùng đang xem để ẩn hoặc cập nhật phiên đó
      if (SessionManager.getCurrentUser().getRole() != UserRole.ADMIN) {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
          if ("home".equals(mainLayout.getCurrentContext())) {
            ServerConnection.sendData(new GetActiveAndWaitingAuctionsRequestDTO());
          } else if ("seller".equals(mainLayout.getCurrentContext())) {
            ServerConnection.sendData(new GetAuctionsBySellerRequestDTO(SessionManager.getCurrentUser().getId()));
          }
        }
      }
    });
  }
}
