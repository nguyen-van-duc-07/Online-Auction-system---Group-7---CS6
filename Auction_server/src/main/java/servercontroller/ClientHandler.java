package servercontroller;

import com.auction.shared.request.*;
import com.auction.shared.response.LoginResponseDTO;
import com.auction.shared.response.UploadItemResponseDTO;
import lombok.Getter;

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
  private Socket clientSocket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  @Getter
  private String authenticatedUserId = null;
  public ClientHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
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
                out.writeObject(RequestHandler.signup(signUpReq));
                out.flush();
              }

              case LoginRequestDTO loginReq -> {
                LoginResponseDTO loginRes = RequestHandler.login(loginReq);
                if (loginRes.isSuccess()) {
                  this.authenticatedUserId = loginRes.getUser().getId();
                  Server.registerClient(this.authenticatedUserId, this);
                  System.out.println("Đăng nhập thành công, đã lưu phiên cho user: " + this.authenticatedUserId);
                }
                out.writeObject(RequestHandler.login(loginReq));
                out.flush();
              }

              case UploadItemRequestDTO uploadItemReq -> {
                if (!isAuthenticated()) break;
                out.writeObject(RequestHandler.uploadItem(uploadItemReq, this.authenticatedUserId));
                out.flush();
              }

              case GetActiveAuctionRequestDTO getActiveAuctionReq -> {
                out.writeObject(RequestHandler.getActiveAuctions(getActiveAuctionReq));
                out.flush();
              }

              case GetAuctionsBySellerRequestDTO getAuctionsBySellerReq -> {
                if (!isAuthenticated()) break;
                out.writeObject(RequestHandler.getAuctionsBySeller(getAuctionsBySellerReq, this.authenticatedUserId));
                out.flush();
              }

              case UpdateProfileRequestDTO updateProfileReq -> {
                if (!isAuthenticated()) break;
                out.writeObject(RequestHandler.updateProfile(updateProfileReq, this.authenticatedUserId));
                out.flush();
              }

              case PlaceBidRequestDTO bidReq -> {
                out.writeObject(RequestHandler.placeBid(bidReq));
                out.flush();
              }

              case JoinRoomRequestDTO joinRoomReq -> {
                Server.joinSelectedAuctionRoom(joinRoomReq.getSelectedAuctionId(), this);
                out.writeObject(RequestHandler.joinRoom(joinRoomReq));
                out.flush();
              }

              case LeaveRoomRequestDTO leaveRoomReq -> {
                Server.leaveSelectedAuctionRoom(leaveRoomReq.getSelectedAuctionId(), this);
              }

              case SellerRegisterRequestDTO sellerRegisterReq -> {
                out.writeObject(RequestHandler.sellerRegister(sellerRegisterReq));
                out.flush();
              }

              case CheckingSellerProfileRequestDTO checkingSellerProfileReq -> {
                out.writeObject(RequestHandler.checkingSellerProfile(checkingSellerProfileReq));
                out.flush();
              }

              case GetSellerProfileRequestDTO getSellerProfileReq -> {
                out.writeObject(RequestHandler.getSellerProfile(getSellerProfileReq));
                out.flush();
              }

              case UpdateSellerProfileStatusRequestDTO updateSellerProfileStatusReq -> {
                out.writeObject(RequestHandler.updateSellerProfileStatus(updateSellerProfileStatusReq));
                out.flush();
              }

              case GetOrderRequestDTO getOrderReq -> {
                out.writeObject(RequestHandler.getOrder(getOrderReq));
                out.flush();
              }
              case SetAutoBidRequestDTO setAutoBidReq -> {
                out.writeObject(RequestHandler.setAutoBid(setAutoBidReq));
                out.flush();
              }

              case CancelAutoBidRequestDTO cancelAutoBidReq -> {
                out.writeObject(RequestHandler.cancelAutoBid(cancelAutoBidReq));
                out.flush();
              }

              default -> {
                System.out.println(">>> Server nhận được Request không xác định!");
              }
            }
          }
        }
        catch(Exception e){
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      Server.removeClientFromAllRooms(this);
      Server.unregisterClient(this.authenticatedUserId);
    }
  }

  private boolean isAuthenticated() {
    if (this.authenticatedUserId == null) {
      System.out.println("Từ chối truy cập: Client chưa đăng nhập!");
      return false;
    }
    return true;
  }

  public void closeConnection() {
    try {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
      if (clientSocket != null) {
        clientSocket.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Phương thức để Server gọi khi cần gửi thông báo (Broadcast).
   * @param response
   */
  public void sendData(Object response) {
    try {
      if (out != null) {
        out.writeObject(response);
        out.flush();
      }
    } catch (Exception e) {
      System.out.println("Lỗi gửi dữ liệu cho Client, Client có thể đã ngắt kết nối.");
    }
  }
}
