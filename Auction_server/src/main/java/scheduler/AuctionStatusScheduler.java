package scheduler;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.response.AuctionStatusUpdateDTO;
import repository.AuctionRepository;
import servercontroller.Server;

import java.time.LocalDateTime;
import java.util.List;
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

    LocalDateTime now = LocalDateTime.now();

    List<String> activateIds = auctionRepo.findAuctionsToActivate(now);
    auctionRepo.activateAuctions(activateIds);
    for (String id : activateIds) {
      System.out.println("BROADCAST ACTIVE: " + id);
      Server.broadcastToAuctionRoom(new AuctionStatusUpdateDTO(id, AuctionStatus.ACTIVE));
    }

    List<String> closeIds = auctionRepo.findAuctionsToClose(now);

    auctionRepo.closeExpiredAuctions(closeIds);

    for (String id : closeIds) {
      System.out.println("BROADCAST CLOSED: " + id);
      Server.broadcastToAuctionRoom(new AuctionStatusUpdateDTO(id, AuctionStatus.CLOSED));
    }
  }
}