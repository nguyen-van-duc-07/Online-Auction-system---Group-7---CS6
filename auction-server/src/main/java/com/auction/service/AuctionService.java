package com.example.auctionserver.service;

import com.example.auctionserver.model.Auction;
import com.example.auctionserver.model.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionService {

    private static AuctionService instance;

    // Lưu trữ các phiên đấu giá (Đang chạy và Đã kết thúc)
    private final Map<String, Auction> auctions;

    // Thread pool chạy ngầm để liên tục kiểm tra xem có phiên nào hết giờ không
    private final ScheduledExecutorService scheduler;

    private AuctionService() {
        this.auctions = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);

        // Cứ mỗi 1 giây, hệ thống sẽ tự quét các phiên đấu giá 1 lần
        startAuctionExpirationChecker();
    }

    public static synchronized AuctionService getInstance() {
        if (instance == null) {
            instance = new AuctionService();
        }
        return instance;
    }


    public void addAuction(Auction auction) {
        auctions.put(auction.getId(), auction);
        System.out.println("Đã thêm phiên đấu giá: " + auction.getItem().getName());
    }

    public Auction getAuction(String auctionId) {
        return auctions.get(auctionId);
    }

    public List<Auction> getAllAuctions() {
        return new ArrayList<>(auctions.values());
    }

    public List<Auction> getActiveAuctions() {
        return auctions.values().stream()
                .filter(a -> a.getStatus() == AuctionStatus.ACTIVE)
                .toList();
    }


    public boolean placeBid(String auctionId, String bidderId, BigDecimal bidAmount) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) throw new IllegalArgumentException("Không tìm thấy phiên đấu giá!");

        synchronized (auction) {
            boolean isSuccess = auction.applyBid(bidderId, bidAmount);

            if (isSuccess) {
                System.out.println("Bidder " + bidderId + " đặt thành công: " + bidAmount.toPlainString());
                return true;
            }
            return false;
        }
    }


    // Thuật toán Anti-sniping (Chống đặt giá trộm phút chót)
    private void handleAntiSniping(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime(); // Nhớ thêm getter trong model

        // Nếu thời gian còn lại ít hơn 5 phút (300 giây)
        if (now.plusMinutes(5).isAfter(endTime)) {
            // Gia hạn thêm 10 phút
            auction.setEndTime(endTime.plusMinutes(10)); // Nhớ thêm setter trong model
            System.out.println("Kích hoạt Anti-sniping! Đã gia hạn thêm thời gian cho: " + auction.getId());
        }
    }

    // Luồng chạy ngầm để tự động đóng phiên đấu giá
    private void startAuctionExpirationChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();

            for (Auction auction : auctions.values()) {
                // Nếu đang ACTIVE mà thời gian hiện tại đã vượt qua thời gian kết thúc
                if (auction.getStatus() == AuctionStatus.ACTIVE && now.isAfter(auction.getEndTime())) {

                    synchronized (auction) {
                        // Double check để chắc chắn
                        if (auction.getStatus() == AuctionStatus.ACTIVE) {
                            auction.setStatus(AuctionStatus.FINISHED); // Nhớ đổi tên status phù hợp với Enum của bạn
                            System.out.println("Đã tự động kết thúc phiên: " + auction.getId() + ". Người thắng: " + auction.getHighestBidderId());

                            // Gửi thông báo người chiến thắng
                            // NotificationService.getInstance().notifyAllClients("AUCTION_ENDED", auction);
                        }
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS); // Quét mỗi giây
    }

    // Khi tắt Server thì phải tắt ThreadPool
    public void shutdown() {
        scheduler.shutdown();
    }
}