package servercontroller;

import com.auction.shared.model.auction.Auction;
import com.auction.shared.response.AuctionExtendedDTO;
import com.auction.shared.response.AuctionResultDTO;
import com.auction.shared.response.NewBidDTO;
import com.auction.shared.response.ResponseDTO;
import scheduler.AuctionStatusScheduler;
import scheduler.OrderExpiryScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class khởi chạy Server.
 */
public class Server {
  private static final Logger log = LoggerFactory.getLogger(Server.class);
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
  private static final Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();

  /**
   * Phương thức ghi thông tin Client vào trong danh sách quản lý của Room đấu giá.
   * @param selectedAuctionId
   * @param client
   */
  public static void joinSelectedAuctionRoom(String selectedAuctionId, ClientHandler client) {
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
      // Khởi chạy HTTP Static File Server cho ảnh sản phẩm
      service.ImageHttpServer.start();

      AuctionStatusScheduler scheduler = new AuctionStatusScheduler();
      scheduler.start();

      OrderExpiryScheduler orderExpiryScheduler = new OrderExpiryScheduler();
      orderExpiryScheduler.start();

      ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
      log.info("Server đã chạy trên port: {}", SERVER_PORT);
      log.info("Đang đợi Client kết nối vào...");
      List<Socket> clientSockets = new ArrayList<>();
      while (true) {
        Socket clientSocket = serverSocket.accept();
        clientSockets.add(clientSocket);
        ClientHandler task = new ClientHandler(clientSocket);
        pool.execute(task);
      }
    } catch (Exception e) {
      log.error("Lỗi nghiêm trọng xảy ra ở Server", e);
    }
  }

  /**
   * Phương thức gửi thông báo cho toàn bộ Client bên trong room khi có bid hợp lệ mới.
   */
  public static void broadcastToAuctionRoom(ResponseDTO responseDTO) {
    if (responseDTO instanceof NewBidDTO newBidDTO) {
      Set<ClientHandler> room = auctionRooms.get(newBidDTO.getAuctionId());
      if (room != null) {
        for (ClientHandler client : room) {
          client.sendData(newBidDTO);
        }
      }
    } else if (responseDTO instanceof AuctionResultDTO auctionResult) {
      Set<ClientHandler> room = auctionRooms.get(auctionResult.getAuctionId());
      if (room != null) {
        for (ClientHandler client : room) {
          client.sendData(auctionResult);
        }
      }
    } else if (responseDTO instanceof AuctionExtendedDTO auctionExtended) {
      Set<ClientHandler> room = auctionRooms.get(auctionExtended.getAuctionId());
      if (room != null) {
        for (ClientHandler client : room) {
          client.sendData(auctionExtended);
        }
      }
    }
  }

  /**
   * Phương thức dùng để xoá các Client bị mất kết nối.
   * @param client
   */
  public static void removeClientFromAllRooms(ClientHandler client) {
    auctionRooms.values().forEach(room -> room.remove(client));
  }

  public static void registerClient(String userId, ClientHandler handler) {
    connectedClients.put(userId, handler);
    log.info("Client đã đăng ký: {}", userId);
  }

  public static void unregisterClient(String userId) {
    if (userId != null) {
      connectedClients.remove(userId);
      log.info("Client đã hủy đăng ký: {}", userId);
    }
  }

  public static void sendToUser(String userId, Object dto) {
    ClientHandler handler = connectedClients.get(userId);
    if (handler != null) {
      handler.sendData(dto);
    } else {
      log.warn("User {} không online, bỏ qua việc gửi dữ liệu.", userId);
    }
  }

  public static void broadcastToAll(ResponseDTO responseDTO) {
    for (ClientHandler client : connectedClients.values()) {
      client.sendData(responseDTO);
    }
  }

  /**
   * Kiểm tra xem tài khoản đã được đăng nhập ở một nơi khác hay chưa.
   * @param userId ID của người dùng
   * @return true nếu đang online, false nếu chưa
   */
  public static boolean isUserOnline(String userId) {
    return connectedClients.containsKey(userId);
  }
}
