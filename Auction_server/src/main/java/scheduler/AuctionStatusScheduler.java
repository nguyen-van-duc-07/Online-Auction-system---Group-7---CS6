package scheduler;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.order.Order;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.response.AuctionResultDTO;
import com.auction.shared.response.AuctionStatusUpdateDTO;
import com.auction.shared.util.CurrencyUtils;
import com.auction.shared.util.NotificationTemplate;
import repository.AuctionRepository;
import servercontroller.Server;
import service.NotificationService;
import service.OrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionStatusScheduler {
  private static final Logger log = LoggerFactory.getLogger(AuctionStatusScheduler.class);

  private final AuctionRepository auctionRepo;
  private final OrderService orderService;
  private final NotificationService notifService;

  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor();

  /**
   * Constructor mặc định cho Production.
   */
  public AuctionStatusScheduler() {
    this(
        new AuctionRepository(),
        new OrderService(),
        new NotificationService()
    );
  }

  /**
   * Constructor nhận tham số phục vụ cho Unit Test.
   */
  public AuctionStatusScheduler(
      AuctionRepository auctionRepo,
      OrderService orderService,
      NotificationService notifService
  ) {
    this.auctionRepo = auctionRepo;
    this.orderService = orderService;
    this.notifService = notifService;
  }

  public void start() {
    scheduler.scheduleAtFixedRate(
        this::updateAuctionStatus,
        0,
        5,
        TimeUnit.SECONDS
    );
  }

  /**
   * Package-private để hỗ trợ Unit Test trực tiếp mà không cần chờ Scheduler.
   */
  void updateAuctionStatus() {
    try {
      LocalDateTime now = LocalDateTime.now();
      List<String> activateIds = auctionRepo.findAuctionsToActivate(now);
      if (!activateIds.isEmpty()) {
        auctionRepo.activateAuctions(activateIds);
        for (String id : activateIds) {
          log.info("BROADCAST ACTIVE: {}", id);
          Server.broadcastToAuctionRoom(new AuctionStatusUpdateDTO(id, AuctionStatus.ACTIVE));

          AuctionResponseDTO auction = auctionRepo.findAuctionResponseDTOById(id);
          if (auction != null) {
            notifService.sendNewAuctionNotification(id, auction.getItem().getName(), auction.getStartPrice());
          }
        }
      }
      Map<String, AuctionResponseDTO> auctionsToClose = auctionRepo.findAuctionsToCloseWithDetails(now);
      if (!auctionsToClose.isEmpty()) {
        for (Map.Entry<String, AuctionResponseDTO> entry : auctionsToClose.entrySet()) {
          String id = entry.getKey();
          // Bọc quá trình đóng phiên đấu giá dưới lock trong bộ nhớ để bảo toàn tính nguyên tử với placeBid
          final boolean[] closedSuccessfully = new boolean[1];
          service.BidService.runWithAuctionLock(id, () -> {
            // 1. Cố gắng cập nhật trạng thái đấu giá sang CLOSED ở DB dưới điều kiện thời gian và trạng thái ACTIVE
            if (auctionRepo.tryCloseExpiredAuction(id, now)) {
              closedSuccessfully[0] = true;
              // 2. An toàn dọn dẹp đối tượng lock tĩnh khỏi bộ nhớ vì trạng thái phiên đã chốt là CLOSED
              service.BidService.removeAuctionLock(id);
            }
          });

          if (!closedSuccessfully[0]) {
            continue;
          }

          AuctionResponseDTO auction = entry.getValue();
          if (auction == null) {
            continue;
          }

          log.info("BROADCAST CLOSED: {}", id);
          Server.broadcastToAuctionRoom(new AuctionStatusUpdateDTO(id, AuctionStatus.CLOSED));
          processAuctionResult(id, auction);
        }
      }
    } catch (Exception e) {
      log.error("[SCHEDULER] Lỗi nghiêm trọng trong chu kỳ quét trạng thái đấu giá", e);
    }
  }


  private void processAuctionResult(String auctionId, AuctionResponseDTO auction) {
    try {
      String itemName = auction.getItem().getName();
      String winnerId = auction.getHighestBidderId();
      String sellerId = auction.getUserId();

      // Trường hợp 1: Có người thắng cuộc
      if (winnerId != null && !winnerId.isBlank()) {
        log.info("SERVER GỬI THÔNG BÁO CHIẾN THẮNG [AuctionId: {} | Winner: {} | Giá cuối: {}]",
            auctionId, winnerId, CurrencyUtils.formatVnd(auction.getCurrentHighestPrice()));

        Server.broadcastToAuctionRoom(new AuctionResultDTO(
            auctionId, winnerId, itemName, auction.getCurrentHighestPrice()
        ));

        Order order = orderService.createOrder(auctionId, winnerId, auction.getCurrentHighestPrice());
        if (order != null) {
          log.info("[SCHEDULER] Kết quả tạo đơn hàng thành công: {}", order.getId());
          // Gửi thông báo cho Winner
          notifService.sendFromNotification(NotificationTemplate.auctionWon(winnerId, itemName, auction.getCurrentHighestPrice(), order.getId()));
          // Gửi thông báo cho Seller
          notifService.sendFromNotification(NotificationTemplate.auctionEndedWithWinner(sellerId, itemName, auction.getCurrentHighestPrice(), order.getId()));
        } else {
          log.error("[SYSTEM ERROR] Phiên {} có người thắng nhưng hệ thống không thể tạo Đơn hàng!", auctionId);
        }
      }
      // Trường hợp 2: Hết giờ nhưng không có ai đặt giá thầu
      else {
        log.info("Phiên {} kết thúc nhưng không có người thắng (Ế hàng).", auctionId);
        notifService.sendFromNotification(NotificationTemplate.auctionEndedNoWinner(sellerId, itemName, auctionId));
      }
    } catch (Exception e) {
      log.error("Lỗi khi xử lý kết quả hậu kỳ cho phiên đấu giá: " + auctionId, e);
    }
  }
}