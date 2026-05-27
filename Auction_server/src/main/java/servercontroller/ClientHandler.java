package servercontroller;

import com.auction.shared.enums.UserRole;
import com.auction.shared.request.*;
import com.auction.shared.response.LoginResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Lớp đại diện cho một luồng (Thread) độc lập xử lý giao tiếp mạng với một Client cụ thể.
 * <p>
 * Lớp này chạy liên tục trong vòng lặp để đọc dữ liệu đầu vào. Khi nhận được một gói tin
 * {@code RequestDTO} hợp lệ, nó sử dụng <i>Switch - case</i> để phân loại,
 * sau đó chuyển tiếp dữ liệu cho {@link RequestHandler} xử lý và trực tiếp gửi đối tượng
 * {@code ResponseDTO} nhận được trả ngược lại qua luồng mạng cho Client.
 * </p>
 *
 * @see com.auction.shared.request.RequestDTO
 */
public class ClientHandler implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
  private Socket clientSocket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private String userId;
  private UserRole role;

  public UserRole getRole() {
    return role;
  }

  public ClientHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
    try {
      this.clientSocket.setKeepAlive(true);
      this.clientSocket.setSoTimeout(30 * 60 * 1000); // Tự động đóng kết nối sau 30 phút rảnh rỗi
    } catch (Exception e) {
      log.warn("Không thể cấu hình thuộc tính Socket cho client: {}", e.getMessage());
    }
  }

  @Override
  public void run() {
    String clientIp = clientSocket.getRemoteSocketAddress().toString();
    MDC.put("clientId", "IP:" + clientIp);
    log.info("Client mới đang kết nối từ IP: {}", clientIp);

    try {
      out = new ObjectOutputStream(clientSocket.getOutputStream());
      out.flush();
      in = new ObjectInputStream(clientSocket.getInputStream());

      Object requestObj;
      while ((requestObj = in.readObject()) != null) {
        try{
          // Đảm bảo dữ liệu được gửi là 1 DTO hợp lệ
          if (requestObj instanceof RequestDTO) {

            // Tự dộng rẽ nhánh và ép kiểu
            switch (requestObj) {
              case SignUpRequestDTO signUpReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.signup(signUpReq));
                  out.flush();
                }
              }

              case LoginRequestDTO loginReq -> {
                LoginResponseDTO response = RequestHandler.login(loginReq);

                if (response.isSuccess()) {
                  String checkUserId = response.getUser().getId();

                  // Kiểm tra xem user này đã có trong danh sách online của Server chưa
                  if (Server.isUserOnline(checkUserId)) {
                    // Nếu đã online, ngắt kết nối cũ để giải phóng tài nguyên và ghi đè phiên đăng nhập mới
                    ClientHandler oldHandler = Server.getConnectedClient(checkUserId);
                    if (oldHandler != null) {
                      log.info("Tài khoản {} đã đăng nhập từ kết nối mới. Đang đóng kết nối cũ...", checkUserId);
                      oldHandler.closeConnection();
                      Server.unregisterClient(checkUserId);
                    }
                  }

                  // Cho phép đăng nhập và lưu kết nối mới vào danh sách Server
                  this.userId = checkUserId;
                  this.role = response.getUser().getRole();
                  MDC.put("clientId", "User:" + this.userId);
                  Server.registerClient(this.userId, this);
                }

                // Trả về đối tượng 'res' đã được xử lý thay vì gọi hàm login() thêm lần nữa
                synchronized (out) {
                  out.writeObject(response);
                  out.flush();
                }
              }

              case LogoutRequestDTO logoutReq -> {
                RequestHandler.logout(logoutReq);
              }

              case UploadItemRequestDTO uploadItemReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.uploadItem(uploadItemReq));
                  out.flush();
                }
              }

              case AuctionRequestDTO request -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.handleFindAuctionById(request));
                  out.flush();
                }
              }

              case GetActiveAuctionsRequestDTO getActiveAuctionReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getActiveAuctions(getActiveAuctionReq));
                  out.flush();
                }
              }

              case GetWaitingAuctionsRequestDTO getWaitingAuctionsReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getWaitingAuctions(getWaitingAuctionsReq));
                  out.flush();
                }
              }

              case GetClosedAuctionsRequestDTO getClosedAuctionsReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getClosedAuctions(getClosedAuctionsReq));
                  out.flush();
                }
              }

              case GetActiveAndWaitingAuctionsRequestDTO getActiveAndWaitingAuctionsReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getActiveAndWaitingAuctions(getActiveAndWaitingAuctionsReq));
                  out.flush();
                }
              }

              case GetActiveAuctionsBySellerRequestDTO getActiveAuctionsBySellerReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getActiveAuctionsBySeller(getActiveAuctionsBySellerReq));
                  out.flush();
                }
              }

              case GetAuctionsBySellerRequestDTO getAuctionsBySellerReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getAuctionsBySeller(getAuctionsBySellerReq));
                  out.flush();
                }
              }

              case UpdateProfileRequestDTO updateProfileReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.updateProfile(updateProfileReq));
                  out.flush();
                }
              }

              case ChangePasswordRequestDTO changePasswordReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.changePassword(changePasswordReq));
                  out.flush();
                }
              }

              case PlaceBidRequestDTO bidReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.placeBid(bidReq));
                  out.flush();
                }
              }

              case JoinRoomRequestDTO joinRoomReq -> {
                Server.joinSelectedAuctionRoom(joinRoomReq.getSelectedAuctionId(), this);
                synchronized (out) {
                  out.writeObject(RequestHandler.joinRoom(joinRoomReq, this.userId));
                  out.flush();
                }
              }

              case LeaveRoomRequestDTO leaveRoomReq -> {
                Server.leaveSelectedAuctionRoom(leaveRoomReq.getSelectedAuctionId(), this);
              }

              case SellerRegisterRequestDTO sellerRegisterReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.sellerRegister(sellerRegisterReq));
                  out.flush();
                }
              }

              case CheckingSellerProfileRequestDTO checkingSellerProfileReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.checkingSellerProfile(checkingSellerProfileReq));
                  out.flush();
                }
              }

              case GetSellerProfileRequestDTO getSellerProfileReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getSellerProfile(getSellerProfileReq));
                  out.flush();
                }
              }

              case UpdateSellerProfileStatusRequestDTO updateSellerProfileStatusReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.updateSellerProfileStatus(updateSellerProfileStatusReq));
                  out.flush();
                }
              }

              case CancelSellerAuctionsRequestDTO cancelSellerAuctionsReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.cancelSellerAuctions(cancelSellerAuctionsReq));
                  out.flush();
                }
              }

              case RestoreSellerAuctionsRequestDTO restoreSellerAuctionsReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.restoreSellerAuctions(restoreSellerAuctionsReq));
                  out.flush();
                }
              }

              case GetOrderRequestDTO getOrderReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getOrder(getOrderReq));
                  out.flush();
                }
              }
              case SetAutoBidRequestDTO setAutoBidReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.setAutoBid(setAutoBidReq));
                  out.flush();
                }
              }

              case CancelAutoBidRequestDTO cancelAutoBidReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.cancelAutoBid(cancelAutoBidReq));
                  out.flush();
                }
              }

              case GetBalanceRequestDTO getBalanceReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getBalance(this.userId));
                  out.flush();
                }
              }

              case GetNotificationsRequestDTO getNotifReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getNotifications(getNotifReq));
                  out.flush();
                }
              }

              case MarkNotificationReadRequestDTO markReadReq -> {
                RequestHandler.markNotificationRead(markReadReq);
                // Không cần trả về response
              }

              case GetPendingOrdersOfSellerRequestDTO request -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.handleGetPendingOrdersOfSeller(request));
                  out.flush();
                }
              }

              case GetPendingOrdersOfBuyerRequestDTO request -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.handleGetPendingOrdersOfBuyer(request));
                  out.flush();
                }
              }

              case CancelOrderRequestDTO request -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.cancelOrder(request));
                  out.flush();
                }
              }

              case ConfirmOrderRequestDTO confirmOrderReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.confirmOrder(confirmOrderReq));
                  out.flush();
                }
              }

              case GetCompletedOrdersOfSellerRequestDTO request -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.handleGetCompletedOrdersOfSeller(request));
                  out.flush();
                }
              }

              case GetCancelledOrdersOfSellerRequestDTO request -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.handleGetCancelledOrdersOfSeller(request));
                  out.flush();
                }
              }

              case GetCompletedOrdersOfBuyerRequestDTO request -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.handleGetCompletedOrdersOfBuyer(request));
                  out.flush();
                }
              }

              case GetCancelledOrdersOfBuyerRequestDTO request -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.handleGetCancelledOrdersOfBuyer(request));
                  out.flush();
                }
              }

              case CreateTransactionRequestDTO req -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.createTransactionRequest(req));
                  out.flush();
                }
              }

              case GetPendingTransactionsRequestDTO req -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getPendingTransactions(req));
                  out.flush();
                }
              }

              case ProcessTransactionRequestDTO req -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.processTransactionRequest(req));
                  out.flush();
                }
              }

              case UpdateAuctionStatusRequestDTO req -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.updateAuctionStatus(req));
                  out.flush();
                }
              }

              case CreateAdminRequestDTO request -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.createAdmin(request));
                  out.flush();
                }
              }

              case GetAllUsersRequestDTO getAllUsersReq -> {
                synchronized (out) {
                  out.writeObject(RequestHandler.getAllUsers(getAllUsersReq));
                  out.flush();
                }
              }

              default -> {
                log.warn("Server nhận được Request không xác định!");
              }
            }
          }
        }
        catch(Exception e){
          log.error("Lỗi xảy ra khi xử lý request từ client", e);
        }
      }
    } catch (java.io.EOFException e) {
      log.info("Client đã ngắt kết nối (đóng ứng dụng).");
    } catch (java.net.SocketException e) {
      log.info("Kết nối Socket với Client bị ngắt đột ngột: {}", e.getMessage());
    } catch (Exception e) {
      log.error("Lỗi nghiêm trọng trong phiên kết nối của Client", e);
    } finally {
      Server.removeClientFromAllRooms(this);
      Server.unregisterClient(this.userId, this);
      log.info("Kết nối client đóng.");
      MDC.clear();
    }
  }

  public void closeConnection() {
    try {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        synchronized (out) {
          out.close();
        }
      }
      if (clientSocket != null) {
        clientSocket.close();
      }
    } catch (Exception e) {
      log.error("Lỗi khi đóng kết nối", e);
    }
  }

  /**
   * Phương thức để Server gọi khi cần gửi thông báo (Broadcast).
   * @param response
   */
  public void sendData(Object response) {
    try {
      if (out != null) {
        synchronized (out) {
          out.writeObject(response);
          out.flush();
        }
      }
    } catch (Exception e) {
      log.warn("Lỗi gửi dữ liệu cho Client, Client có thể đã ngắt kết nối.");
    }
  }
}
