package com.auction.client.network;

import com.auction.shared.network.NetworkConfig;
import com.auction.shared.response.*;

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
  private static Socket socket;
  private static ObjectOutputStream out;
  private static ObjectInputStream in;

  public static void connect() {
    try {
      socket = new Socket(NetworkConfig.DEFAULT_HOST, NetworkConfig.SERVER_PORT);

      out = new ObjectOutputStream(socket.getOutputStream());
      out.flush();
      in = new ObjectInputStream(socket.getInputStream());

      new Thread(() -> listenForData()).start();

    } catch (IOException e) {
      e.printStackTrace();
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

            case GetActiveAuctionResponseDTO getActiveAuctionRes ->
                ResponseHandler.handleGetActiveAuctions(getActiveAuctionRes);

            case GetAuctionsBySellerResponseDTO getAuctionsBySellerRes ->
              ResponseHandler.handleGetAuctionsBySeller(getAuctionsBySellerRes);

            case UpdateProfileResponseDTO updateProfileRes -> ResponseHandler.handleUpdateProfile(updateProfileRes);

            case AuctionStatusUpdateDTO dto -> System.out.println("CLIENT RECEIVED: " + dto.getId()
                + " status=" + dto.getAuctionStatus());

            case NewBidDTO dto -> {
              ResponseHandler.handleNewBid(dto);
              System.out.println("CLIENT RECEIVED: Phien: " + dto.getAuctionId()
                  + "- Bidder: " + dto.getBidderId()
                  + "dat gia: " + dto.getBidAmount());
            }

            case PlaceBidResponseDTO dto -> {
              System.out.println("Ket qua: " + dto.getMessage());
              ResponseHandler.handlePlaceBidResponse(dto); // THÊM DÒNG NÀY
            }

            case AuctionResponseDTO auctionRes ->
                ResponseHandler.handleAuctionRoomJoined(auctionRes); // THÊM CASE NÀY

            case PaymentNotificationDTO dto -> {
              System.out.println("[CLIENT] Nhận PaymentNotification: " + dto.getItemName());
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

            case OrderActionResponseDTO dto ->
                ResponseHandler.handleOrderAction(dto);

            case GetOrderResponseDTO dto ->
                ResponseHandler.handleGetOrder(dto);

            case OrderUpdateNotificationDTO dto ->
                ResponseHandler.handleOrderUpdateNotification(dto);

            case AuctionPriceUpdateDTO dto ->
                ResponseHandler.handleAuctionPriceUpdate(dto);

            default -> System.out.println("Phản hồi không hợp lệ");
          }
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public static void sendData(Object obj) {
    try {
      if (out != null) {
        out.writeObject(obj);
        out.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
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
      e.printStackTrace();
    }
  }
}
