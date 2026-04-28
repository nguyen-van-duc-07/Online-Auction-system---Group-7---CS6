package service;

import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.item.Item;
import com.auction.shared.enums.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionService {

    // ========================
    // Singleton (Holder)
    // ========================
    private AuctionService() {}

    private static class Holder {
        private static final AuctionService INSTANCE = new AuctionService();
    }

    public static AuctionService getInstance() {
        return Holder.INSTANCE;
    }

    // ========================
    // Storage
    // ========================
    private final Map<String, Auction> auctions = new ConcurrentHashMap<>();

    // ========================
    // Create Auction
    // ========================
    public Auction createAuction(Item item,
                                 BigDecimal startPrice,
                                 LocalDateTime startTime,
                                 LocalDateTime endTime) {

        Auction auction = new Auction(item, startPrice, startTime, endTime);

        auctions.put(auction.getId(), auction);

        return auction;
    }

    // ========================
    // Start Auction
    // ========================
    public void startAuction(String auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) return;

        if (auction.getStatus() != AuctionStatus.WAITING) return;

        auction.start();
    }

    // ========================
    // Place Bid
    // ========================
    public boolean placeBid(String auctionId,
                            String bidderId,
                            BigDecimal amount) {

        Auction auction = auctions.get(auctionId);
        if (auction == null) return false;

        return auction.applyBid(bidderId, amount);
    }

    // ========================
    // Cancel Auction
    // ========================
    public void cancelAuction(String auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) return;

        auction.cancel();
    }

    // ========================
    // Get Auction
    // ========================
    public Auction getAuction(String auctionId) {
        return auctions.get(auctionId);
    }

    // ========================
    // Auto close expired auctions
    // ========================
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();

        for (Auction auction : auctions.values()) {
            if (auction.getStatus() == AuctionStatus.ACTIVE &&
                    now.isAfter(auction.getEndTime())) {

                auction.close();
                System.out.println("Auction closed: " + auction.getId());
            }
        }
    }
}