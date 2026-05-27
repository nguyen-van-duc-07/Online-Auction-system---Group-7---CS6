package scheduler;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.enums.NotificationType;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.order.Order;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.response.AuctionResultDTO;
import com.auction.shared.response.AuctionStatusUpdateDTO;
import com.auction.shared.util.NotificationTemplate;
import repository.AuctionRepository;
import repository.SellerProfileRepository;
import servercontroller.Server;
import service.NotificationService;
import service.OrderService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionStatusScheduler {
  private static final Logger log = LoggerFactory.getLogger(AuctionStatusScheduler.class);

  private final AuctionRepository auctionRepo =
      new AuctionRepository();

  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor();
  private final OrderService orderService = new OrderService();
  private final NotificationService notifService = new NotificationService();
  private final SellerProfileRepository sellerProfileRepo = new SellerProfileRepository();
  public void start() {

    scheduler.scheduleAtFixedRate(
        this::updateAuctionStatus,
        0,
        5,
        TimeUnit.SECONDS
    );
  }

  private void updateAuctionStatus() {

    try {
      LocalDateTime now = LocalDateTime.now();
      List<String> activateIds = auctionRepo.findAuctionsToActivate(now);
      auctionRepo.activateAuctions(activateIds);
      for (String id : activateIds) {
        log.info("BROADCAST ACTIVE: {}", id);
        Server.broadcastToAuctionRoom(new AuctionStatusUpdateDTO(id, AuctionStatus.ACTIVE));

        AuctionResponseDTO auction = auctionRepo.findAuctionResponseDTOById(id);
        if (auction != null) {
          notifService.sendNewAuctionNotification(id, auction.getItem().getName(), auction.getStartPrice());
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

          AuctionResponseDTO auction = auctionRepo.findAuctionResponseDTOById(id);
          if (auction == null) {
            continue;
          }

          log.info("BROADCAST CLOSED: {}", id);
          Server.broadcastToAuctionRoom(new AuctionStatusUpdateDTO(id, AuctionStatus.CLOSED));
          String itemId = auction.getItem().getId();
          String itemName = auction.getItem().getName();
          String winnerId = auction.getHighestBidderId();

          String sellerId = auction.getSellerId();
          if (winnerId != null && !winnerId.isBlank()) {
            log.info("SERVER GỬI THÔNG BÁO CHIẾN THẮNG [AuctionId: {} | Winner: {} | Giá cuối : {}]",
                id, winnerId, com.auction.shared.util.FormatUtil.fmt(auction.getCurrentHighestPrice()));
            Server.broadcastToAuctionRoom(new AuctionResultDTO(
                id,
                winnerId,
                itemId,
                itemName,
                auction.getCurrentHighestPrice()
            ));
            Order order = orderService.createOrder(id, winnerId, auction.getCurrentHighestPrice());
            log.info("[SCHEDULER] Kết quả tạo đơn hàng: {}", (order != null ? order.getId() : "NULL"));
            if (order != null) {
              // Thông báo cho winner
              notifService.sendFromNotification(
                  NotificationTemplate.auctionWon(
                      winnerId,
                      itemName,
                      auction.getCurrentHighestPrice(),
                      order.getId()
                  )
              );

              // Thông báo cho seller
              notifService.sendFromNotification(
                  NotificationTemplate.auctionEndedWithWinner(
                      sellerId,
                      itemName,
                      auction.getCurrentHighestPrice(),
                      order.getId()
                  )
              );
            } else {
              log.info("Phiên {} không có người thắng.", id);
              notifService.sendFromNotification(
                  NotificationTemplate.auctionEndedNoWinner(
                      sellerId,
                      itemName,
                      id
                  )
              );
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("[SCHEDULER] Lỗi nghiêm trọng khi cập nhật trạng thái đấu giá", e);
    }
  }
}