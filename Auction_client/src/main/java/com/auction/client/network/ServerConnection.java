package com.auction.client.network;

import com.auction.shared.network.NetworkConfig;
import com.auction.shared.response.AuctionExtendedDTO;
import com.auction.shared.response.AuctionPriceUpdateDTO;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.response.AuctionResultDTO;
import com.auction.shared.response.AuctionStatusUpdateDTO;
import com.auction.shared.response.AutoBidDefeatedDTO;
import com.auction.shared.response.AutoBidResponseDTO;
import com.auction.shared.response.CancelSellerAuctionsResponseDTO;
import com.auction.shared.response.ChangePasswordResponseDTO;
import com.auction.shared.response.CheckingSellerProfileResponseDTO;
import com.auction.shared.response.CreateAdminResponseDTO;
import com.auction.shared.response.CreateTransactionResponseDTO;
import com.auction.shared.response.DeleteNotificationResponseDTO;
import com.auction.shared.response.GetActiveAndWaitingAuctionsResponseDTO;
import com.auction.shared.response.GetAllUsersResponseDTO;
import com.auction.shared.response.GetAuctionsBySellerResponseDTO;
import com.auction.shared.response.GetAuctionsResponseDTO;
import com.auction.shared.response.GetBalanceResponseDTO;
import com.auction.shared.response.GetNotificationsResponseDTO;
import com.auction.shared.response.GetOrderResponseDTO;
import com.auction.shared.response.GetOrdersResponseDTO;
import com.auction.shared.response.GetPendingTransactionsResponseDTO;
import com.auction.shared.response.GetSellerProfileResponseDTO;
import com.auction.shared.response.JoinRoomResponseDTO;
import com.auction.shared.response.LoginResponseDTO;
import com.auction.shared.response.NewBidDTO;
import com.auction.shared.response.NotificationDTO;
import com.auction.shared.response.OrderActionResponseDTO;
import com.auction.shared.response.OrderUpdateNotificationDTO;
import com.auction.shared.response.PaymentNotificationDTO;
import com.auction.shared.response.PlaceBidResponseDTO;
import com.auction.shared.response.ProcessTransactionResponseDTO;
import com.auction.shared.response.ResponseDTO;
import com.auction.shared.response.RestoreSellerAuctionsResponseDTO;
import com.auction.shared.response.SellerRegisterResponseDTO;
import com.auction.shared.response.SignUpResponseDTO;
import com.auction.shared.response.UpdateAuctionStatusResponseDTO;
import com.auction.shared.response.UpdateProfileResponseDTO;
import com.auction.shared.response.UpdateSellerProfileStatusResponseDTO;
import com.auction.shared.response.UploadItemResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Quản lý kết nối mạng (Socket) từ phía Client tới Server.
 * <p>
 * Lớp này chịu trách nhiệm thiết lập kết nối, duy trì luồng lắng nghe dữ liệu
 * liên tục (chạy trên một Thread riêng), và gửi các đối tượng dữ liệu qua mạng.
 * Khi nhận được một {@code ResponseDTO} từ Server, nó sử dụng Switch Pattern Matching
 * để tự động phân loại và điều hướng dữ liệu đến {@link ResponseHandler} xử lý.
 * </p>
 *
 * @see com.auction.shared.response.ResponseDTO
 * @see ResponseHandler
 */
public class ServerConnection {
  private static final Logger log = LoggerFactory.getLogger(ServerConnection.class);
  private static Socket socket;
  private static ObjectOutputStream out;
  private static ObjectInputStream in;

  /**
   * Thiết lập kết nối Socket tới Server và khởi chạy luồng lắng nghe dữ liệu.
   *
   * @throws IOException nếu xảy ra lỗi I/O khi kết nối hoặc khởi tạo luồng dữ liệu
   */
  public static synchronized void connect() throws IOException {
    log.info("Đang kết nối tới máy chủ tại {}:{}", NetworkConfig.DEFAULT_HOST, NetworkConfig.SERVER_PORT);
    socket = new Socket(NetworkConfig.DEFAULT_HOST, NetworkConfig.SERVER_PORT);
    socket.setKeepAlive(true); // Kích hoạt TCP Keep-Alive

    out = new ObjectOutputStream(socket.getOutputStream());
    out.flush();
    in = new ObjectInputStream(socket.getInputStream());

    Thread listenerThread = new Thread(() -> listenForData());
    listenerThread.setDaemon(true);
    listenerThread.start();
    log.info("Kết nối tới máy chủ thành công.");
  }

  /**
   * Đảm bảo rằng kết nối tới Server vẫn hoạt động, nếu không sẽ tự động kết nối lại.
   *
   * @throws IOException nếu xảy ra lỗi khi thử kết nối lại
   */
  public static synchronized void ensureConnected() throws IOException {
    if (socket == null || socket.isClosed() || !socket.isConnected() || out == null || in == null) {
      log.warn("Mất kết nối hoặc chưa kết nối tới máy chủ. Đang kết nối lại...");
      closeConnection();
      connect();
    }
  }

  private static void listenForData() {
    try {
      Object response;
      while ((response = in.readObject()) != null) {
        if (response instanceof ResponseDTO) {
          switch (response) {
            case LoginResponseDTO loginRes -> ResponseHandler.login(loginRes);

            case SignUpResponseDTO signUpRes -> ResponseHandler.signUp(signUpRes);

            case UploadItemResponseDTO uploadItemRes -> ResponseHandler.handleUploadItem(uploadItemRes);

            case AuctionResponseDTO auctionRes -> ResponseHandler.handleFindAuctionById(auctionRes);

            case GetAuctionsResponseDTO getActiveAuctionRes ->
                ResponseHandler.handleGetAuctions(getActiveAuctionRes);

            case GetActiveAndWaitingAuctionsResponseDTO getActiveAndWaitingAuctionsRes ->
              ResponseHandler.handleGetActiveAndWaitingAuctions(getActiveAndWaitingAuctionsRes);

            case GetAuctionsBySellerResponseDTO getAuctionsBySellerRes ->
              ResponseHandler.handleGetAuctionsBySeller(getAuctionsBySellerRes);

            case UpdateProfileResponseDTO updateProfileRes -> ResponseHandler.handleUpdateProfile(updateProfileRes);

            case ChangePasswordResponseDTO changePasswordRes ->
                ResponseHandler.handleChangePassword(changePasswordRes);

            case AuctionStatusUpdateDTO dto -> {
              log.info("CLIENT RECEIVED: {} status={}", dto.getId(), dto.getAuctionStatus());
              ResponseHandler.handleAuctionStatusUpdate(dto);
            }

            case NewBidDTO dto -> {
              ResponseHandler.handleNewBid(dto);
              log.info("CLIENT RECEIVED: Phien: {} - Bidder: {} dat gia: {}", 
                  dto.getAuctionId(), dto.getBidderId(), dto.getBidAmount());
            }

            case PlaceBidResponseDTO dto -> {
              log.info("Ket qua: {}", dto.getMessage());
              ResponseHandler.handlePlaceBidResponse(dto);
            }

            case JoinRoomResponseDTO joinRoomRes ->
                ResponseHandler.handleAuctionRoomJoined(joinRoomRes);

            case PaymentNotificationDTO dto -> {
              log.info("[CLIENT] Nhận PaymentNotification: {}", dto.getItemName());
                ResponseHandler.handlePaymentNotification(dto);
            }

            case AuctionResultDTO dto ->
                ResponseHandler.handleAuctionResult(dto);

            case SellerRegisterResponseDTO SellerRegisterRes ->
                ResponseHandler.handleSellerRegister(SellerRegisterRes);

            case CheckingSellerProfileResponseDTO checkingSellerProfileRes ->
                ResponseHandler.checkingSellerProfile(checkingSellerProfileRes);

            case GetSellerProfileResponseDTO getSellerProfileRes ->
                ResponseHandler.handleGetSellerProfile(getSellerProfileRes);

            case UpdateSellerProfileStatusResponseDTO updateSellerProfileStatusRes ->
                ResponseHandler.handleUpdateSellerProfileStatus(updateSellerProfileStatusRes);

            case CancelSellerAuctionsResponseDTO cancelSellerAuctionsRes ->
                ResponseHandler.handleCancelSellerAuctions(cancelSellerAuctionsRes);

            case RestoreSellerAuctionsResponseDTO restoreSellerAuctionsRes ->
                ResponseHandler.handleRestoreSellerAuctions(restoreSellerAuctionsRes);

            case OrderActionResponseDTO dto ->
                ResponseHandler.handleOrderAction(dto);

            case GetOrderResponseDTO dto ->
                ResponseHandler.handleGetOrder(dto);

            case OrderUpdateNotificationDTO dto ->
                ResponseHandler.handleOrderUpdateNotification(dto);

            case AuctionPriceUpdateDTO dto ->
                ResponseHandler.handleAuctionPriceUpdate(dto);

            case AutoBidResponseDTO dto ->
                ResponseHandler.handleAutoBidResponse(dto);

            case AutoBidDefeatedDTO dto -> {
              log.info("[CLIENT] Nhận thông báo Bot bị đè giá: {}", dto.getMessage());
              ResponseHandler.handleAutoBidDefeated(dto);
            }

            case GetBalanceResponseDTO balanceRes -> {
              log.info("Nhận phản hồi lấy số dư từ Server.");
              ResponseHandler.handleGetBalance(balanceRes);
            }

            case GetNotificationsResponseDTO dto ->
                ResponseHandler.handleGetNotifications(dto);

            case NotificationDTO dto ->
                ResponseHandler.handleNewNotification(dto);

            case AuctionExtendedDTO dto ->
                ResponseHandler.handleAuctionExtended(dto);

            case GetOrdersResponseDTO responseDTO -> {
              ResponseHandler.handleGetOrders(responseDTO);
            }

            case CreateTransactionResponseDTO responseDTO -> {
              ResponseHandler.handleCreateTransactionResponse(responseDTO);
            }

            case GetPendingTransactionsResponseDTO responseDTO -> {
              ResponseHandler.handleGetPendingTransactionsResponse(responseDTO);
            }

            case ProcessTransactionResponseDTO responseDTO -> {
              ResponseHandler.handleProcessTransactionResponse(responseDTO);
            }

            case UpdateAuctionStatusResponseDTO responseDTO -> {
              ResponseHandler.handleUpdateAuctionStatus(responseDTO);
            }

            case GetAllUsersResponseDTO responseDTO -> {
              ResponseHandler.handleGetAllUsers(responseDTO);
            }

            case CreateAdminResponseDTO responseDTO -> {
              ResponseHandler.handleCreateAdmin(responseDTO);
            }

            case DeleteNotificationResponseDTO responseDTO -> {
              ResponseHandler.handleDeleteNotification(responseDTO);
            }

            default -> log.warn("Phản hồi không hợp lệ");
          }
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      if (socket != null && socket.isClosed()) {
        log.info("Đã đóng kết nối với Server thành công.");
      } else {
        boolean wasLoggedIn = (com.auction.client.network.SessionManager.currentUser != null);
        log.error("Lỗi khi lắng nghe dữ liệu từ Server (hoặc bị ngắt kết nối): {}", e.getMessage());
        closeConnection(); // Tự dọn dẹp socket khi có lỗi đường truyền

        if (wasLoggedIn) {
          com.auction.client.network.SessionManager.currentUser = null;
          com.auction.client.network.SessionManager.currentAuctionId = null;
          com.auction.client.network.SessionManager.currentOrderId = null;

          javafx.application.Platform.runLater(() -> {
            com.auction.client.screenhandler.ScreenController.clearHistory();
            com.auction.client.screenhandler.ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");
            javafx.application.Platform.runLater(() -> {
              com.auction.client.screenhandler.ScreenController.showAlert(
                  javafx.scene.control.Alert.AlertType.WARNING,
                  "Thông báo kết nối",
                  "Tài khoản của bạn đã được đăng nhập ở một thiết bị khác hoặc mất kết nối tới máy chủ!"
              );
            });
          });
        }
      }
    }
  }

  /**
   * Gửi một đối tượng dữ liệu (Request DTO) lên Server qua kết nối Socket.
   *
   * @param obj đối tượng dữ liệu cần gửi
   */
  public static void sendData(Object obj) {
    try {
      ensureConnected();
      ObjectOutputStream tempOut = out;
      if (tempOut != null) {
        synchronized (tempOut) {
          tempOut.writeObject(obj);
          tempOut.flush();
        }
      } else {
        throw new IOException("Luồng xuất dữ liệu (out) bị null.");
      }
    } catch (IOException e) {
      log.error("Lỗi khi gửi dữ liệu lên Server", e);
      closeConnection(); // Reset connection
      
      javafx.application.Platform.runLater(() -> {
        com.auction.client.screenhandler.ScreenController.showAlert(
            javafx.scene.control.Alert.AlertType.ERROR,
            "Lỗi kết nối",
            "Mất kết nối tới máy chủ. Vui lòng kiểm tra lại đường truyền mạng hoặc khởi động lại ứng dụng!"
        );
      });
    }
  }

  /**
   * Đóng an toàn kết nối Socket và các luồng I/O liên quan, dọn dẹp tài nguyên.
   */
  public static synchronized void closeConnection() {
    try {
      if (in != null) {
        in.close();
      }
    } catch (IOException e) {
      log.error("Lỗi khi đóng luồng vào", e);
    } finally {
      in = null;
    }

    try {
      if (out != null) {
        out.close();
      }
    } catch (IOException e) {
      log.error("Lỗi khi đóng luồng ra", e);
    } finally {
      out = null;
    }

    try {
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      log.error("Lỗi khi đóng socket", e);
    } finally {
      socket = null;
    }
    log.info("Đã dọn dẹp và đóng kết nối hoàn toàn.");
  }
}
