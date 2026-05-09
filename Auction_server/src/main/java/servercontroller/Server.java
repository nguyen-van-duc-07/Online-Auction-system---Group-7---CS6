package servercontroller;

import com.auction.shared.response.ResponseDTO;
import scheduler.AuctionStatusScheduler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class khởi chạy Server.
 */
public class Server {
  public static int SERVER_PORT = 8080;
  private static final ExecutorService pool = Executors.newFixedThreadPool(10);
  // Luu danh sach clients
  public static List<ClientHandler> clients = new ArrayList<>();
  public static void main(String[] args) {
    try {
      AuctionStatusScheduler scheduler = new AuctionStatusScheduler();
      scheduler.start();
      ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
      System.out.println(">>> SERVER DA CHAY TREN PORT: " + SERVER_PORT);
      System.out.println(">>> DANG DOI CLIENT KET NOI VAO ...");
      List<Socket> clientSockets = new ArrayList<>();
      while (true) {
        Socket clientSocket = serverSocket.accept();
        clientSockets.add(clientSocket);
        ClientHandler task = new ClientHandler(clientSocket);
        clients.add(task);
        pool.execute(task);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  // Thong bao khi co cac su kien (gia moi, phien moi) cho cac client
  public static void broadcast(ResponseDTO response) {

    for (ClientHandler client : clients) {
      client.sendResponse(response);
    }
  }
}
