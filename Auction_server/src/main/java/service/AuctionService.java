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
  public static boolean uploadNewItem(UploadItemRequestDTO request, String sellerProfileId) {
    // Tạo và lưu Item
    Item item = new Item(request.getItemName(), request.getItemType(), request.getDescription());

    boolean isItemSaved = itemRepo.saveItem(item);
    if (!isItemSaved) {
      System.out.println("Unable to save item");
      return false; // Nếu lưu Item thất bại thì dừng quy trình
    }
    return getInstance().createAuction(item,
        sellerProfileId,
        request.getStartPrice(),
        request.getMinStepPrice(),
        request.getStartTime(),
        request.getEndTime());
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
                               LocalDateTime endTime) {

    Auction auction = new Auction(item, startPrice, minStepPrice, startTime, endTime);

    boolean isAuctionSaved = auctionRepo.saveAuction(auction, sellerId);

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
  public boolean placeBid(String auctionId, String bidderId, BigDecimal amount) {

    // 1. LẤY TỪ DATABASE: Tránh lỗi RAM bị trắng khi restart Server
    Auction auction = auctionRepo.findAuctionById(auctionId);
    if (auction == null) {
      System.out.println(">>> Lỗi: Không tìm thấy Auction trong DB!");
      return false;
    }

    // Ngăn chặn người bán tự đấu giá sản phẩm của chính mình
    SellerProfileRepository sellerRepo = new SellerProfileRepository();
    String bidderSellerProfileId = sellerRepo.findProfileIdByUserId(bidderId);
    if (bidderSellerProfileId != null && bidderSellerProfileId.equals(auction.getSellerId())) {
      System.out.println(">>> Lỗi: Người bán không thể tự đấu giá sản phẩm của chính mình!");
      return false;
    }

    // 2. NGƯỜI THẬT ĐẶT GIÁ
    boolean isSuccess = auction.applyBid(bidderId, amount);

    if (isSuccess) {
      // LUÔN LUÔN lưu giao dịch của người thật vào DB trước
      auctionRepo.updatePrice(auctionId, bidderId, amount);
      bidRepo.saveBid(new BidTransaction(auctionId, bidderId, amount));

      String humanName = repository.UserRepository.getUserFullName(bidderId);

      // 3. KIỂM TRA BOT PHẢN ĐÒN
      repository.AutoBidConfigRepository autoBidRepo = new repository.AutoBidConfigRepository();
      List<com.auction.shared.model.auction.AutoBidConfig> activeBots = autoBidRepo.findActiveBotsOrderedByMaxPrice(auctionId);

      // NẾU CÓ BOT VÀ CHỦ BOT KHÔNG PHẢI LÀ NGƯỜI VỪA ĐẶT GIÁ
      if (!activeBots.isEmpty() && !activeBots.get(0).getUserId().equals(bidderId)) {
        com.auction.shared.model.auction.AutoBidConfig bot = activeBots.get(0);
        BigDecimal stepAmount = auction.getMinStepPrice();

        // KỊCH BẢN A: BOT CÒN TIỀN -> PHẢN ĐÒN (JUMP CALCULATION)
        if (bot.getMaxPrice().compareTo(amount) > 0) {

          BigDecimal priceToBeat = amount.add(stepAmount);
          BigDecimal newBotPrice = priceToBeat.min(bot.getMaxPrice());

          repository.WalletRepository walletRepo = new repository.WalletRepository();
          com.auction.shared.model.user.Wallet wallet = walletRepo.getWalletByUserId(bot.getUserId());
          BigDecimal requiredFreeze = newBotPrice.multiply(new BigDecimal("0.1")); // Cần 10% để đóng băng

          // Nếu ví Bot đủ tiền
          if (wallet != null && wallet.getBalance().compareTo(requiredFreeze) >= 0) {
            // Lưu Bot vào DB (O(1) Jump)
            auctionRepo.updatePrice(auctionId, bot.getUserId(), newBotPrice);
            bidRepo.saveBid(new BidTransaction(auctionId, bot.getUserId(), newBotPrice));

            // Phát sóng cho phòng
            servercontroller.Server.broadcastToAuctionRoom(
                    new com.auction.shared.response.NewBidDTO(auctionId, bidderId, humanName, amount));

            String botName = repository.UserRepository.getUserFullName(bot.getUserId());
            servercontroller.Server.broadcastToAuctionRoom(
                    new com.auction.shared.response.NewBidDTO(auctionId, bot.getUserId(), "[Auto] " + botName, newBotPrice));
          }
          // Nếu ví Bot KHÔNG đủ tiền (Dù cấu hình Max Price vẫn còn)
          else {
            autoBidRepo.deactivate(bot.getId(), auctionId);
            String fomoMessage = "Bot của bạn đã bị tắt do số dư ví không đủ 10% (" + requiredFreeze + " VNĐ) để tiếp tục đè giá!";
            servercontroller.Server.sendToUser(bot.getUserId(),
                    new com.auction.shared.response.AutoBidDefeatedDTO(auctionId, fomoMessage));

            // Chỉ gửi thông báo người thật chiến thắng
            servercontroller.Server.broadcastToAuctionRoom(
                    new com.auction.shared.response.NewBidDTO(auctionId, bidderId, humanName, amount));
          }
        }
        // 4. KHÔNG CÓ BOT -> ĐẤU GIÁ THỦ CÔNG BÌNH THƯỜNG
        else {
          servercontroller.Server.broadcastToAuctionRoom(
                  new com.auction.shared.response.NewBidDTO(auctionId, bidderId, humanName, amount));
        }
      }
    }
    return isSuccess;
  }

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
  public static void resolveAutoBidFight(String auctionId) {
    repository.AutoBidConfigRepository autoBidRepo = new repository.AutoBidConfigRepository();

    // 1. Lấy danh sách các Bot đang BẬT trong phòng này, SẮP XẾP GIÁ TỐI ĐA GIẢM DẦN
    List<com.auction.shared.model.auction.AutoBidConfig> activeBots = autoBidRepo.findActiveBotsOrderedByMaxPrice(auctionId);

    if (activeBots.size() < 2) {
      return; // Chưa có đối thủ, Bot đang độc tôn, không cần đánh nhau
    }

    com.auction.shared.model.auction.AutoBidConfig winnerBot = activeBots.get(0); // Top 1 (Đại gia nhiều tiền nhất)
    com.auction.shared.model.auction.AutoBidConfig loserBot = activeBots.get(1);  // Top 2 (Người thua cuộc)

    // Lấy thông tin phiên đấu giá hiện tại để tính toán
    Auction currentAuction = auctionRepo.findAuctionById(auctionId);
    if (currentAuction == null) return;

    // Nếu giá hiện tại đã cao hơn cả Top 1 rồi thì bỏ qua (Phòng hờ lỗi logic)
    if (currentAuction.getCurrentHighestPrice().compareTo(winnerBot.getMaxPrice()) >= 0) {
      return;
    }

    // 2. THUẬT TOÁN JUMP CALCULATION: Mức giá để thắng = M2 + S
    BigDecimal stepAmount = currentAuction.getMinStepPrice();
    BigDecimal priceToBeat = loserBot.getMaxPrice().add(stepAmount);

    // Nếu giá để thắng vô tình vượt quá ngân sách M1 của Winner, thì Winner chỉ chốt ở giới hạn M1
    BigDecimal newHighestPrice = priceToBeat.min(winnerBot.getMaxPrice());

    // 3. CẬP NHẬT TRẠNG THÁI DATABASE VÀ OBJECT
    currentAuction.setCurrentHighestPrice(newHighestPrice);
    currentAuction.setHighestBidderId(winnerBot.getUserId());

    // Cập nhật giá mới vào bảng Auctions
    auctionRepo.updatePrice(auctionId, winnerBot.getUserId(), newHighestPrice);

    // Lưu lịch sử giao dịch để vẽ biểu đồ cho Bot 1
    bidRepo.saveBid(new BidTransaction(auctionId, winnerBot.getUserId(), newHighestPrice));

    // 4. "GIẾT" BOT CỦA NGƯỜI THUA VÀ GỬI THÔNG BÁO FOMO
    for (int i = 1; i < activeBots.size(); i++) {
      com.auction.shared.model.auction.AutoBidConfig defeatedBot = activeBots.get(i);

      // Tắt trạng thái active trong DB
      autoBidRepo.deactivate(defeatedBot.getId(), auctionId);

      // Bắn tín hiệu "Báo tử" (AutoBidDefeatedDTO) về riêng cho Client của người thua
      String fomoMessage = "Tài phiệt khác đã dùng Bot đè bẹp ngân sách của bạn! Kéo xuống nâng giá ngay để giành lại top 1!";

      // Bắn gói tin về thông qua hàm sendToClient (bạn cần đảm bảo hàm này có tồn tại trong lớp Server)
      servercontroller.Server.sendToUser(defeatedBot.getUserId(),
              new com.auction.shared.response.AutoBidDefeatedDTO(auctionId, fomoMessage));
    }

    // 5. BROADCAST MỨC GIÁ MỚI CHO TOÀN BỘ PHÒNG
    // Lấy thêm tên user để hiển thị lên UI người dẫn đầu
    String winnerName = repository.UserRepository.getUserFullName(winnerBot.getUserId());

    com.auction.shared.response.NewBidDTO finalBidResult =
            new com.auction.shared.response.NewBidDTO(auctionId, winnerBot.getUserId(), winnerName, newHighestPrice);

    servercontroller.Server.broadcastToAuctionRoom(finalBidResult);
  }
}
