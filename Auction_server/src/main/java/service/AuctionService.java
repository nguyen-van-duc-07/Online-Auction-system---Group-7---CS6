package service;

import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.item.Item;
import com.auction.shared.model.item.ItemDTO;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.request.UploadItemRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;

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
    // Tạo và lưu Item
    Item item = new Item(request.getItemName(), request.getItemType(), request.getDescription());

    boolean isItemSaved = itemRepo.saveItem(item);
    if (!isItemSaved) {
      System.out.println("Unable to save item");
      return false; // Nếu lưu Item thất bại thì dừng quy trình
    }

    // Lưu ảnh sản phẩm lên đĩa cứng (nếu có)
    String imagePath = null;
    if (request.getImageBytes() != null && request.getImageBytes().length > 0) {
      try {
        imagePath = ImageStorageService.saveImage(request.getImageBytes(), request.getImageExtension());
      } catch (Exception e) {
        System.err.println("Lỗi lưu ảnh, tiếp tục tạo auction không có ảnh: " + e.getMessage());
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
      return true;
    } else {
      return false;
    }
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
    SellerProfileRepository sellerRepo = new SellerProfileRepository();
    String sellerId = sellerRepo.findProfileIdByUserId(userId);
    List<AuctionDTO> activeAuctionsBelongsToSeller = auctionRepo.findActiveAuctionsBySellerId(sellerId);
    return activeAuctionsBelongsToSeller;
  }

  public static List<AuctionDTO> getAuctionsBySeller(String userId) {
    SellerProfileRepository sellerRepo = new SellerProfileRepository();
    String sellerId = sellerRepo.findProfileIdByUserId(userId);
    List<AuctionDTO> auctionsBelongsToSeller = auctionRepo.findAuctionsBySellerId(sellerId);
    return auctionsBelongsToSeller;
  }

  public static boolean cancelActiveAndWaitingAuctionsBySellerUserId(String userId) {
    SellerProfileRepository sellerRepo = new SellerProfileRepository();
    String sellerId = sellerRepo.findProfileIdByUserId(userId);
    if (sellerId == null) {
      return false;
    }
    return auctionRepo.cancelActiveAndWaitingAuctionsBySellerId(sellerId);
  }

  public static boolean restoreCanceledAuctionsBySellerUserId(String userId) {
    SellerProfileRepository sellerRepo = new SellerProfileRepository();
    String sellerId = sellerRepo.findProfileIdByUserId(userId);
    if (sellerId == null) {
      return false;
    }
    return auctionRepo.restoreCanceledAuctionsBySellerId(sellerId, LocalDateTime.now());
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
  /**
   * Giải quyết đụng độ khi có nhiều người dùng cùng bật Auto-bid trong một phòng.
   * Sử dụng thuật toán Jump Calculation (Đấu giá giá lớn thứ hai) để chốt giá cuối cùng
   * bằng công thức O(1), tránh việc tạo vòng lặp đè giá gây quá tải server.
   *
   * @param auctionId ID của phiên đấu giá cần kiểm tra và phân định Auto-bid
   */
}
