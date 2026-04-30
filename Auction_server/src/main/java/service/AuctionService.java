package service;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.item.Item;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lớp AuctionService xử lý logic nghiệp vụ của hệ thống đấu giá.
 * Bao gồm các chức năng như tạo phiên đấu giá, xử lý đặt giá, và quản lý trạng thái đấu giá.
 */
public class AuctionService {

  // ========================
  // Singleton (Holder)
  // ========================
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
   * Tạo một phiên đấu giá mới cho một sản phẩm.
   *
   * @param item sản phẩm được đưa vào đấu giá
   * @param startPrice giá khởi điểm của phiên đấu giá
   * @param startTime thời gian bắt đầu đấu giá
   * @param endTime thời gian kết thúc đấu giá
   * @return đối tượng Auction vừa được tạo
   */
  public Auction createAuction(Item item,
                               BigDecimal startPrice,
                               LocalDateTime startTime,
                               LocalDateTime endTime) {

    Auction auction = new Auction(item, startPrice, startTime, endTime);

    auctions.put(auction.getId(), auction);

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

    if (auction.getStatus() != AuctionStatus.WAITING) {
      return;
    }

    auction.start();
  }

  /**
   * Thực hiện đặt giá (bid) cho một phiên đấu giá.
   *
   * <p>Hệ thống sẽ kiểm tra:
   * - Phiên đấu giá có tồn tại hay không
   * - Sau đó chuyển yêu cầu đặt giá cho đối tượng Auction xử lý</p>
   *
   * @param auctionId mã phiên đấu giá
   * @param bidderId mã người đặt giá
   * @param amount số tiền đặt giá
   * @return true nếu đặt giá thành công, false nếu thất bại
   */
  public boolean placeBid(String auctionId,
                          String bidderId,
                          BigDecimal amount) {

    Auction auction = auctions.get(auctionId);
    if (auction == null) {
      return false;
    }
    return auction.applyBid(bidderId, amount);
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
   * Tự động đóng các phiên đấu giá đã hết hạn.
   *
   * <p>Hệ thống sẽ duyệt toàn bộ danh sách auction và:
   * - Kiểm tra các auction đang ở trạng thái ACTIVE
   * - Nếu thời gian hiện tại đã vượt quá endTime thì sẽ đóng auction</p>
   *
   * <p>Sau khi đóng, hệ thống sẽ in thông báo ra console.</p>
   */
  public void closeExpiredAuctions() {
    LocalDateTime now = LocalDateTime.now();

    for (Auction auction : auctions.values()) {
      if (auction.getStatus() == AuctionStatus.ACTIVE
          && now.isAfter(auction.getEndTime())) {

        auction.close();
        System.out.println("Auction closed: " + auction.getId());
      }
    }
  }
}