package service;

import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.item.Item;
import com.auction.shared.model.item.ItemDTO;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.request.UploadItemRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.request.UpdateAuctionStatusRequestDTO;
import com.auction.shared.response.UpdateAuctionStatusResponseDTO;
import com.auction.shared.response.AuctionStatusUpdateDTO;
import servercontroller.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import repository.AuctionRepository;
import repository.BidTransactionRepository;
import repository.ItemRepository;
import repository.SellerProfileRepository;

/**
 * Lớp AuctionService xử lý logic nghiệp vụ của hệ thống đấu giá.
 * Bao gồm các chức năng như tạo phiên đấu giá, xử lý đặt giá, và quản lý trạng thái đấu giá.
 */
public class AuctionService {
  private static final Logger log = LoggerFactory.getLogger(AuctionService.class);

  private static final ItemRepository itemRepo = new ItemRepository();
  private static final AuctionRepository auctionRepo = new AuctionRepository();
  private static final BidTransactionRepository bidRepo = new BidTransactionRepository();
  private AuctionService() {
  }

  private static class Holder {
    private static final AuctionService INSTANCE = new AuctionService();
  }

  public static AuctionService getInstance() {
    return Holder.INSTANCE;
  }

  private final Map<String, Auction> auctions = new ConcurrentHashMap<>();

  /**
   * Thực hiện luồng đăng bán sản phẩm mới.
   *
   * <p>Tạo đối tượng Item, lưu vào DB. Nếu thành công, tiếp tục tạo Auction và lưu vào DB.</p>
   * * @param req Gói tin DTO chứa thông tin cấu hình từ Client
   *
   * @return {@code true} nếu cả Item và Auction đều được lưu thành công, ngược lại là {@code false}
   */
  public static boolean uploadNewItem(UploadItemRequestDTO request) {
    // Không sử dụng ItemRepository nữa, gộp chung thông tin item vào Auction
    Item item = new Item(request.getItemName(), request.getItemType(), request.getDescription(), request.getAdditionalAttributes());

    // Lưu ảnh sản phẩm lên đĩa cứng (nếu có)
    String imagePath = null;
    if (request.getImageBytes() != null && request.getImageBytes().length > 0) {
      try {
        imagePath = ImageStorageService.saveImage(request.getImageBytes(), request.getImageExtension());
      } catch (Exception e) {
        log.error("Lỗi lưu ảnh, tiếp tục tạo auction không có ảnh: {}", e.getMessage(), e);
      }
    }

    return getInstance().createAuction(item,
        request.getSellerId(),
        request.getStartPrice(),
        request.getMinStepPrice(),
        request.getStartTime(),
        request.getEndTime(),
        imagePath);
  }

  /**
   * Tạo một phiên đấu giá mới cho một sản phẩm.
   *
   * @param item       sản phẩm được đưa vào đấu giá
   * @param startPrice giá khởi điểm của phiên đấu giá
   * @param startTime  thời gian bắt đầu đấu giá
   * @param endTime    thời gian kết thúc đấu giá
   * @return đối tượng Auction vừa được tạo
   */
  public boolean createAuction(Item item,
                               String sellerId,
                               BigDecimal startPrice,
                               BigDecimal minStepPrice,
                               LocalDateTime startTime,
                               LocalDateTime endTime,
                               String imagePath) {

    Auction auction = new Auction(item, sellerId, startPrice, minStepPrice, startTime, endTime);

    boolean isAuctionSaved = auctionRepo.saveAuction(auction, imagePath);

    if (isAuctionSaved) {
      auctions.put(auction.getId(), auction);
      if (auction.getStatus() == AuctionStatus.ACTIVE) {
        new NotificationService().sendNewAuctionNotification(
            auction.getId(),
            auction.getItem().getName(),
            auction.getStartPrice()
        );
      }
      return true;
    } else {
      return false;
    }
  }

  public static AuctionResponseDTO findAuctionById(String id) {
    AuctionResponseDTO auction = auctionRepo.findAuctionById(id);
    return auction;
  }

  /**
   * Bắt đầu một phiên đấu giá nếu hợp lệ.
   *
   * <p>Phiên đấu giá chỉ được bắt đầu khi:
   * - Tồn tại auction theo auctionId
   * - Trạng thái hiện tại là WAITING</p>
   *
   * @param auctionId mã định danh của phiên đấu giá cần bắt đầu
   */
  public void startAuction(String auctionId) {
    Auction auction = auctions.get(auctionId);
    if (auction == null) {
      return;
    }

    auction.start();
  }

  /**
   * Thực hiện đặt giá (bid) cho một phiên đấu giá.
   * Đồng thời áp dụng Jump Calculation để xử lý phản đòn của Bot ngay lập tức trong O(1),
   * triệt tiêu hoàn toàn tình trạng bão thông báo khi Người đấu với Bot.
   *
   * @param auctionId mã phiên đấu giá
   * @param bidderId  mã người đặt giá
   * @param amount    số tiền đặt giá
   * @return true nếu đặt giá thành công, false nếu thất bại
   */
  /**
   * Thực hiện đặt giá (bid) cho một phiên đấu giá.
   * Lấy dữ liệu chuẩn từ DB, lưu lịch sử người thật, và kích hoạt Bot phản đòn O(1).
   */

  /**
   * Hủy một phiên đấu giá theo mã định danh.
   *
   * <p>Nếu phiên đấu giá tồn tại, hệ thống sẽ chuyển trạng thái
   * của auction sang CANCELLED.</p>
   *
   * @param auctionId mã định danh của phiên đấu giá cần hủy
   */
  public void cancelAuction(String auctionId) {
    Auction auction = auctions.get(auctionId);
    if (auction == null) {
      return;
    }

    auction.cancel();
  }

  /**
   * Lấy thông tin phiên đấu giá theo mã định danh.
   *
   * @param auctionId mã định danh của phiên đấu giá
   * @return đối tượng Auction tương ứng, hoặc null nếu không tồn tại
   */
  public Auction getAuction(String auctionId) {
    return auctions.get(auctionId);
  }

  /**
   * Truy xuất danh sách các phiên đấu giá đang mở (ACTIVE) từ cơ sở dữ liệu
   * và chuyển đổi (mapping) chúng sang định dạng DTO cho Client.
   *
   * <p>Mục đích của việc chuyển đổi từ thực thể {@link Auction} nguyên bản sang
   * {@link AuctionResponseDTO} là để lược bỏ các thông tin nhạy cảm và dư thừa
   * (như lịch sử đặt giá chi tiết, thông tin người bán). Việc này giúp giảm tải
   * dung lượng gói tin gửi qua Socket, tối ưu hóa băng thông mạng và tăng tốc độ
   * tải trang chủ cho người dùng.</p>
   *
   * @return Danh sách các đối tượng {@link AuctionResponseDTO} chứa thông tin tóm tắt
   * của các sản phẩm đang được đấu giá trên sàn.
   */
  public static List<AuctionDTO> getActiveAuctionsForClient() {
    List<AuctionDTO> activeAuctions = auctionRepo.findActiveAuctions();
    return activeAuctions;
  }

  public static List<AuctionDTO> getWaitingAuctionsForClient() {
    List<AuctionDTO> waitingAuctions = auctionRepo.findWaitingAuctions();
    return waitingAuctions;
  }

  public static List<AuctionDTO> getClosedAuctionsForClient() {
    List<AuctionDTO> closedAuctions = auctionRepo.findClosedAuctions();
    return closedAuctions;
  }

  /**
   * Truy xuất danh sách các phiên đấu giá đang mở (ACTIVE) và sắp diễn ra (WAITING)
   * từ cơ sở dữ liệu và chuyển đổi (mapping) chúng sang định dạng DTO cho Client.
   *
   * <p>Mục đích của việc chuyển đổi từ thực thể {@link Auction} nguyên bản sang
   * {@link AuctionDTO} là để lược bỏ các thông tin nhạy cảm và dư thừa
   * (như lịch sử đặt giá chi tiết, thông tin người bán). Việc này giúp giảm tải
   * dung lượng gói tin gửi qua Socket, tối ưu hóa băng thông mạng và tăng tốc độ
   * tải trang chủ cho người dùng.</p>
   *
   * @return Danh sách các đối tượng {@link AuctionDTO} chứa thông tin tóm tắt
   * của các sản phẩm đang được đấu giá trên sàn.
   */
  public static List<AuctionDTO> getActiveAndWaitingAuctions() {
    List<AuctionDTO> activeAndWaitingAuctions = auctionRepo.findActiveAndWaitingAuctions();
    return activeAndWaitingAuctions;
  }

  public static List<AuctionDTO> getActiveAuctionsBySeller(String userId) {
    return auctionRepo.findActiveAuctionsBySellerId(userId);
  }

  public static List<AuctionDTO> getAuctionsBySeller(String userId) {
    return auctionRepo.findAuctionsBySellerId(userId);
  }

  public static boolean cancelActiveAndWaitingAuctionsBySellerUserId(String userId) {
    return auctionRepo.cancelActiveAndWaitingAuctionsBySellerId(userId);
  }

  public static boolean restoreCanceledAuctionsBySellerUserId(String userId) {
    return auctionRepo.restoreCanceledAuctionsBySellerId(userId, LocalDateTime.now());
  }

  public static AuctionResponseDTO getAuctionHistory(String auctionId) {
    // 1. Lấy thông tin cơ bản của phiên đấu giá từ AuctionRepository
    AuctionResponseDTO auction = auctionRepo.findAuctionResponseDTOById(auctionId);

    if (auction != null) {
      // 2. Lấy 20 giao dịch gần nhất từ BidTransactionRepository
      // Việc giới hạn (limit) được thực hiện ngay trong SQL để tối ưu
      List<BidTransaction> history = bidRepo.findRecentByAuctionId(auctionId, 20);

      // 3. Gán vào attribute bidHistory của đối tượng Auction
      auction.setBidHistory(history);
    }

    return auction;
  }

  /**
   * Tự động đóng các phiên đấu giá đã hết hạn.
   *
   * <p>Hệ thống sẽ duyệt toàn bộ danh sách auction và:
   * - Kiểm tra các auction đang ở trạng thái ACTIVE
   * - Nếu thời gian hiện tại đã vượt quá endTime thì sẽ đóng auction</p>
   *
   * <p>Sau khi đóng, hệ thống sẽ in thông báo ra console.</p>
   */
  public void closeExpiredAuctions() {
    for (Auction auction : auctions.values()) {
      if (auction.isExpired()) {
        auction.close();
      }
    }
  }

  public static UpdateAuctionStatusResponseDTO updateAuctionStatusByAdmin(UpdateAuctionStatusRequestDTO request) {
    String auctionId = request.getAuctionId();
    AuctionStatus targetStatus = request.getStatus();

    final UpdateAuctionStatusResponseDTO[] response = new UpdateAuctionStatusResponseDTO[1];
    service.BidService.runWithAuctionLock(auctionId, () -> {
      try {
        AuctionResponseDTO auction = auctionRepo.findAuctionById(auctionId);
        if (auction == null) {
          response[0] = new UpdateAuctionStatusResponseDTO(false, "Phiên đấu giá không tồn tại!");
          return;
        }

        if (targetStatus == AuctionStatus.ACTIVE) {
          if (auction.getStatus() == AuctionStatus.ACTIVE) {
            response[0] = new UpdateAuctionStatusResponseDTO(false, "Phiên hiện đang mở sẵn!");
            return;
          }
          if (auction.getStatus() != AuctionStatus.WAITING) {
            response[0] = new UpdateAuctionStatusResponseDTO(false, "Chỉ có thể mở phiên ở trạng thái chờ (WAITING)!");
            return;
          }

          boolean success = auctionRepo.updateAuctionStatusAndStartTime(auctionId, AuctionStatus.ACTIVE.name(), LocalDateTime.now());
          if (success) {
            Auction ramAuction = getInstance().getAuction(auctionId);
            if (ramAuction != null) {
              ramAuction.setStatus(AuctionStatus.ACTIVE);
              ramAuction.setStartTime(LocalDateTime.now());
            }
            Server.broadcastToAll(new AuctionStatusUpdateDTO(auctionId, AuctionStatus.ACTIVE));
            new NotificationService().sendNewAuctionNotification(
                auctionId,
                auction.getItem().getName(),
                auction.getStartPrice()
            );
            response[0] = new UpdateAuctionStatusResponseDTO(true, "Mở phiên đấu giá thành công!");
          } else {
            response[0] = new UpdateAuctionStatusResponseDTO(false, "Mở phiên đấu giá thất bại!");
          }
        } 
        else if (targetStatus == AuctionStatus.CLOSED) {
          if (auction.getStatus() == AuctionStatus.ACTIVE) {
            boolean success = auctionRepo.updateAuctionEndTime(auctionId, LocalDateTime.now());
            if (success) {
              Auction ramAuction = getInstance().getAuction(auctionId);
              if (ramAuction != null) {
                ramAuction.setEndTime(LocalDateTime.now());
              }
              Server.broadcastToAll(new AuctionStatusUpdateDTO(auctionId, AuctionStatus.CLOSED));
              response[0] = new UpdateAuctionStatusResponseDTO(true, "Đóng phiên thành công! Phiên sẽ xử lý kết quả ngay lập tức.");
            } else {
              response[0] = new UpdateAuctionStatusResponseDTO(false, "Đóng phiên thất bại!");
            }
          } else if (auction.getStatus() == AuctionStatus.WAITING) {
            boolean success = auctionRepo.cancelAuctionAndReleaseDeposit(auctionId);
            if (success) {
              Auction ramAuction = getInstance().getAuction(auctionId);
              if (ramAuction != null) {
                ramAuction.setStatus(AuctionStatus.CANCELED);
              }
              Server.broadcastToAll(new AuctionStatusUpdateDTO(auctionId, AuctionStatus.CANCELED));
              response[0] = new UpdateAuctionStatusResponseDTO(true, "Đóng phiên thành công (Đã chuyển trạng thái sang Hủy do phiên chưa bắt đầu)!");
            } else {
              response[0] = new UpdateAuctionStatusResponseDTO(false, "Đóng phiên thất bại!");
            }
          } else {
            response[0] = new UpdateAuctionStatusResponseDTO(false, "Không thể đóng phiên đấu giá ở trạng thái này!");
          }
        } 
        else if (targetStatus == AuctionStatus.CANCELED) {
          if (auction.getStatus() == AuctionStatus.ACTIVE || auction.getStatus() == AuctionStatus.WAITING) {
            boolean success = auctionRepo.cancelAuctionAndReleaseDeposit(auctionId);
            if (success) {
              Auction ramAuction = getInstance().getAuction(auctionId);
              if (ramAuction != null) {
                ramAuction.setStatus(AuctionStatus.CANCELED);
              }
              Server.broadcastToAll(new AuctionStatusUpdateDTO(auctionId, AuctionStatus.CANCELED));
              response[0] = new UpdateAuctionStatusResponseDTO(true, "Chặn và hủy phiên đấu giá thành công, đã hoàn trả cọc!");
            } else {
              response[0] = new UpdateAuctionStatusResponseDTO(false, "Chặn phiên đấu giá thất bại!");
            }
          } else {
            response[0] = new UpdateAuctionStatusResponseDTO(false, "Không thể chặn phiên đấu giá ở trạng thái này!");
          }
        } 
        else {
          response[0] = new UpdateAuctionStatusResponseDTO(false, "Trạng thái yêu cầu không hợp lệ!");
        }
      } catch (Exception e) {
        log.error("Lỗi khi admin cập nhật trạng thái phiên đấu giá ID: {}", auctionId, e);
        response[0] = new UpdateAuctionStatusResponseDTO(false, "Lỗi hệ thống: " + e.getMessage());
      }
    });

    return response[0];
  }

  /**
   * Giải quyết đụng độ khi có nhiều người dùng cùng bật Auto-bid trong một phòng.
   * Sử dụng thuật toán Jump Calculation (Đấu giá giá lớn thứ hai) để chốt giá cuối cùng
   * bằng công thức O(1), tránh việc tạo vòng lặp đè giá gây quá tải server.
   *
   * @param auctionId ID của phiên đấu giá cần kiểm tra và phân định Auto-bid
   */
}
