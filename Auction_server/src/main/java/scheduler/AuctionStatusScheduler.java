package scheduler;

import repository.AuctionRepository;

import java.time.LocalDateTime;
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

    auctionRepo.activateAuctions(now);

    auctionRepo.closeExpiredAuctions(now);

    System.out.println(
        "Checked auction status at: " + now
    );
  }
}