package scheduler;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.response.AuctionResultDTO;
import com.auction.shared.response.AuctionStatusUpdateDTO;
import com.auction.shared.response.PaymentNotificationDTO;
import repository.AuctionRepository;
import repository.debug.Format;
import servercontroller.Server;

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
      Server.broadcastToAuctionRoom(id, new AuctionStatusUpdateDTO(id, AuctionStatus.ACTIVE));
    }

    Map<String, Auction> auctionsToClose = auctionRepo.findAuctionsToCloseWithDetails(now);
    if (!auctionsToClose.isEmpty()) {
      auctionRepo.closeExpiredAuctions(new ArrayList<>(auctionsToClose.keySet()));

      for (Map.Entry<String, Auction> entry : auctionsToClose.entrySet()) {
        String id      = entry.getKey();
        Auction auction = entry.getValue();

        System.out.println("BROADCAST CLOSED: " + id);
        Server.broadcastToAuctionRoom(id, new AuctionStatusUpdateDTO(id, AuctionStatus.CLOSED));

        String winnerId = auction.getHighestBidderId();
        if (winnerId != null && !winnerId.isBlank()) {
          System.out.println("SERVER GUI THONG BAO CHIEN THANG "
              + " [AuctionId: " + id
              + " | Winner: " + winnerId
              + " | Giá cuối : " + Format.fmt(auction.getCurrentHighestPrice()));
          Server.broadcastToAuctionRoom(id, new AuctionResultDTO(
              id,
              winnerId,
              auction.getCurrentHighestPrice()
          ));
          PaymentNotificationDTO paymentDTO = new PaymentNotificationDTO(
              id,
              auction.getItem().getName(),
              auction.getCurrentHighestPrice(),
              winnerId
          );
          Server.sendToUser(winnerId, paymentDTO);
          System.out.println("PAYMENT SENT TO: " + winnerId);
        } else {
          System.out.println("Phiên " + id + " không có người thắng.");
        }
      }
    }
  } catch (Exception e) {
      System.out.println("[SCHEDULER] LỖI: " + e.getMessage());
      e.printStackTrace();
    }
  }
}