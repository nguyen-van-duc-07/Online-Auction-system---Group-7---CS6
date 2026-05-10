package servercontroller;

import com.auction.shared.response.NewBidDTO;
import com.auction.shared.response.ResponseDTO;
import scheduler.AuctionStatusScheduler;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class khởi chạy Server.
 */
public class Server {
  public static int SERVER_PORT = 8080;
  private static final ExecutorService pool = Executors.newFixedThreadPool(10);

  /**
   * Biến kiểu Map để quản lý các Room đấu giá.
   *
   * <p>Key: auctionId (Mã phiên đấu giá)</p>
   *
   * <p>Value: Tập hợp các luồng kết nối (ClientHandler) đang xem phiên đó</p>
   */
  private static final ConcurrentHashMap<String, Set<ClientHandler>> auctionRooms = new ConcurrentHashMap<>();

  /**
   * Phương thức ghi thông tin Client vào trong danh sách quản lý của Room đấu giá.
   * @param selectedAuctionId
   * @param client
   */
  public static void joinSeclectedAuctionRoom(String selectedAuctionId, ClientHandler client) {
    auctionRooms.computeIfAbsent(selectedAuctionId, k -> ConcurrentHashMap.newKeySet()).add(client);
  }

  /**
   * Phương thức xoá thông tin Client ra khỏi danh sách quản lý của Room đấu giá.
   * @param selectedAuctionId
   * @param client
   */
  public static void leaveSelectedAuctionRoom(String selectedAuctionId, ClientHandler client) {
    Set<ClientHandler> room = auctionRooms.get(selectedAuctionId);
    if (room != null) {
      room.remove(client);
      if (room.isEmpty()) {
        auctionRooms.remove(selectedAuctionId);
      }
    }
  }

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
        pool.execute(task);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Phương thức gửi thông báo cho toàn bộ Client bên trong room khi có bid hợp lệ mới.
   */
  public static void broadcastToAuctionRoom(ResponseDTO responseDTO) {
    if (responseDTO instanceof NewBidDTO) {
      NewBidDTO newBidDTO = (NewBidDTO) responseDTO;
      Set<ClientHandler> room = auctionRooms.get(newBidDTO.getAuctionId());
      if (room != null) {
        for (ClientHandler client : room) {
          client.sendData(newBidDTO);
        }
      }
    }
  }

  /**
   * Phương thức dùng để xoá các Client bị mất kế nối.
   * @param client
   */
  public static void removeClientFromAllRooms(ClientHandler client) {
    auctionRooms.values().forEach(room -> room.remove(client));
  }
}
