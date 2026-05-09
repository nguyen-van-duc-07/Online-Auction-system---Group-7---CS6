package servercontroller;

import com.auction.shared.request.*;
import com.auction.shared.response.UploadItemResponseDTO;

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

        // Đảm bảo dữ liệu được gửi là 1 DTO hợp lệ
        if (requestObj instanceof RequestDTO) {

          // Tự dộng rẽ nhánh và ép kiểu
          switch (requestObj) {
            case SignUpRequestDTO signUpReq -> {
              out.writeObject(RequestHandler.signup(signUpReq));
              out.flush();
            }

            case LoginRequestDTO loginReq -> {
              out.writeObject(RequestHandler.login(loginReq));
              out.flush();
            }

            case UploadItemRequestDTO uploadItemReq -> {
              out.writeObject(RequestHandler.uploadItem(uploadItemReq));
              out.flush();
            }

            case GetActiveAuctionRequestDTO getActiveAuctionReq -> {
              out.writeObject(RequestHandler.getActiveAuctions(getActiveAuctionReq));
              out.flush();
            }

            case UpdateProfileRequestDTO updateProfileReq -> {
              out.writeObject(RequestHandler.updateProfile(updateProfileReq));
              out.flush();
            }
            case PlaceBidRequestDTO bidReq -> {

              out.writeObject(
                  RequestHandler.placeBid(
                      bidReq
                  )
              );

              out.flush();
            }

            default -> {
              System.out.println(">>> Server nhận được Request không xác định!");
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
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
  public void sendResponse(Object response) {
    try {
      out.writeObject(response);
      out.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
