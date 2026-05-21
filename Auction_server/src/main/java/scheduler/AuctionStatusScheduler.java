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

public class AuctionStatusScheduler {

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
        System.out.println("BROADCAST ACTIVE: " + id);
        Server.broadcastToAuctionRoom(new AuctionStatusUpdateDTO(id, AuctionStatus.ACTIVE));
      }
      Map<String, AuctionResponseDTO> auctionsToClose = auctionRepo.findAuctionsToCloseWithDetails(now);
      if (!auctionsToClose.isEmpty()) {
        auctionRepo.closeExpiredAuctions(new ArrayList<>(auctionsToClose.keySet()));
        for (Map.Entry<String, AuctionResponseDTO> entry : auctionsToClose.entrySet()) {
          String id      = entry.getKey();
          AuctionResponseDTO auction = entry.getValue();

          System.out.println("BROADCAST CLOSED: " + id);
          Server.broadcastToAuctionRoom(new AuctionStatusUpdateDTO(id, AuctionStatus.CLOSED));
          String itemId = auction.getItem().getId();
          String itemName = auction.getItem().getName();
          String winnerId = auction.getHighestBidderId();

          String sellerProfileId = auction.getSellerId();
          String sellerUserId = sellerProfileRepo.getUserIdByProfileId(sellerProfileId);
          if (winnerId != null && !winnerId.isBlank()) {
            System.out.println("SERVER GUI THONG BAO CHIEN THANG "
                + " [AuctionId: " + id
                + " | Winner: " + winnerId
                + " | Giá cuối : " + com.auction.shared.util.FormatUtil.fmt(auction.getCurrentHighestPrice()));
            Server.broadcastToAuctionRoom(new AuctionResultDTO(
                id,
                winnerId,
                itemId,
                itemName,
                auction.getCurrentHighestPrice()
            ));
            Order order = orderService.createOrder(id, winnerId, auction.getCurrentHighestPrice());
            System.out.println("[SCHEDULER] createOrder result: " + (order != null ? order.getId() : "NULL"));
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
                      sellerUserId,
                      itemName,
                      auction.getCurrentHighestPrice(),
                      order.getId()
                  )
              );
            } else {
              System.out.println("Phiên " + id + " không có người thắng.");
              notifService.sendFromNotification(
                  NotificationTemplate.auctionEndedNoWinner(
                      sellerUserId,
                      itemName,
                      id
                  )
              );
            }
          }
        }
      }
    } catch (Exception e) {
      System.out.println("[SCHEDULER] LỖI: " + e.getMessage());
      e.printStackTrace();
    }
  }
}