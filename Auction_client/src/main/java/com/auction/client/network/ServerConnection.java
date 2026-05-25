package com.auction.client.network;

import com.auction.shared.network.NetworkConfig;
import com.auction.shared.response.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public static void connect() {
    try {
      socket = new Socket(NetworkConfig.DEFAULT_HOST, NetworkConfig.SERVER_PORT);

      out = new ObjectOutputStream(socket.getOutputStream());
      out.flush();
      in = new ObjectInputStream(socket.getInputStream());

      Thread listenerThread = new Thread(() -> listenForData());
      listenerThread.setDaemon(true);
      listenerThread.start();

    } catch (IOException e) {
      log.error("Lỗi khi kết nối tới Server", e);
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

            case GetActiveAuctionsResponseDTO getActiveAuctionRes ->
                ResponseHandler.handleGetActiveAuctions(getActiveAuctionRes);

            case GetWaitingAuctionsResponseDTO getWaitingAuctionsRes ->
                ResponseHandler.handleGetWaitingAuctions(getWaitingAuctionsRes);

            case GetClosedAuctionsResponseDTO getClosedAuctionsRes ->
                ResponseHandler.handleGetClosedAuctions(getClosedAuctionsRes);

            case GetActiveAndWaitingAuctionsResponseDTO getActiveAndWaitingAuctionsRes ->
              ResponseHandler.handleGetActiveAndWaitingAuctions(getActiveAndWaitingAuctionsRes);

            case GetActiveAuctionsBySellerResponseDTO getActiveAuctionsBySellerRes ->
              ResponseHandler.handleGetActiveAuctionsBySelelr(getActiveAuctionsBySellerRes);

            case GetAuctionsBySellerResponseDTO getAuctionsBySellerRes ->
              ResponseHandler.handleGetAuctionsBySeller(getAuctionsBySellerRes);

            case UpdateProfileResponseDTO updateProfileRes -> ResponseHandler.handleUpdateProfile(updateProfileRes);

            case AuctionStatusUpdateDTO dto -> log.info("CLIENT RECEIVED: {} status={}", 
                dto.getId(), dto.getAuctionStatus());

            case NewBidDTO dto -> {
              ResponseHandler.handleNewBid(dto);
              log.info("CLIENT RECEIVED: Phien: {} - Bidder: {} dat gia: {}", 
                  dto.getAuctionId(), dto.getBidderId(), dto.getBidAmount());
            }

            case PlaceBidResponseDTO dto -> {
              log.info("Ket qua: {}", dto.getMessage());
              ResponseHandler.handlePlaceBidResponse(dto); // THÊM DÒNG NÀY
            }

            case JoinRoomResponseDTO joinRoomRes ->
                ResponseHandler.handleAuctionRoomJoined(joinRoomRes); // THÊM CASE NÀY

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
              com.auction.client.network.SessionManager.updateBalance(balanceRes);
              ResponseHandler.handleGetBalance(balanceRes);
            }

            case GetNotificationsResponseDTO dto ->
                ResponseHandler.handleGetNotifications(dto);

            case NotificationDTO dto ->
                ResponseHandler.handleNewNotification(dto);

            case GetPendingOrdersOfSellerResponseDTO responseDTO -> {
              ResponseHandler.handleGetPendingOrdersOfSeller(responseDTO);
            }

            case GetPendingOrdersOfBuyerResponseDTO responseDTO -> {
              ResponseHandler.handleGetPendingOrdersOfBuyer(responseDTO);
            }
            case AuctionExtendedDTO dto ->
                ResponseHandler.handleAuctionExtended(dto);


            case GetCompletedOrdersOfBuyerResponseDTO responseDTO -> {
              ResponseHandler.handleGetCompletedOrdersOfBuyer(responseDTO);
            }

            case GetCancelledOrdersOfBuyerResponseDTO responseDTO -> {
              ResponseHandler.handleGetCancelledOrdersOfBuyer(responseDTO);
            }

            case GetCompletedOrdersOfSellerResponseDTO responseDTO -> {
              ResponseHandler.handleGetCompletedOrdersOfSeller(responseDTO);
            }

            case GetCancelledOrdersOfSellerResponseDTO responseDTO -> {
              ResponseHandler.handleGetCancelledOrdersOfSeller(responseDTO);
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

            default -> log.warn("Phản hồi không hợp lệ");
          }
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      if (socket != null && socket.isClosed()) {
        log.info("Đã đóng kết nối với Server thành công.");
      } else {
        log.error("Lỗi khi lắng nghe dữ liệu từ Server", e);
      }
    }
  }

  public static void sendData(Object obj) {
    try {
      if (out != null) {
        out.writeObject(obj);
        out.flush();
      }
    } catch (IOException e) {
      log.error("Lỗi khi gửi dữ liệu lên Server", e);
    }
  }

  public static void closeConnection() {
    try {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      log.error("Lỗi khi đóng kết nối", e);
    }
  }
}
