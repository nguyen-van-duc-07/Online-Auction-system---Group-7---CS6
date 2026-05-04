package servercontroller;

import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.User;
import com.auction.shared.model.user.UserDTO;
import service.AuthService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Class có nhiệm vụ xử lý khi có người dùng mới kết nối.
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

      Object request;
      while ((request = in.readObject()) != null) {
        // Phân loại yêu cầu dựa trên String
        if (request instanceof String) {
          String action = (String) request;

          // Nếu String nhận được là LOGIN
          if ("LOGIN".equals(action)) {
            // SỬA: Hứng UserDTO và khởi tạo đối tượng User (Bidder)
            UserDTO loginDto = (UserDTO) in.readObject();
            User loginUser = new Bidder(loginDto);

            String answer = RequestHandler.login(loginUser);
            // Gửi 1 String về trước để thông báo rằng đây là phản hồi về yêu cầu login
            out.writeObject("LOGIN_RESPONSE");
            out.writeObject(answer);
            out.flush();

            // Nếu String nhận được là SIGN_UP
          } else if ("SIGN_UP".equals(action)) {
            // SỬA: Hứng UserDTO và khởi tạo đối tượng User (Bidder)
            UserDTO signupDto = (UserDTO) in.readObject();
            User signupUser = new Bidder(signupDto);

            String answer = RequestHandler.signup(signupUser);
            // Gửi 1 String về trước để thông báo rằng đây là phản hồi về yêu cầu signup
            out.writeObject("SIGN_UP_RESPONSE");
            out.writeObject(answer);
            out.flush();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void closeConnection() {
    try {
      if (in != null) in.close();
      if (out != null) out.close();
      if (clientSocket != null) clientSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}