package com.auction.server.service;

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
  private AuctionService() {
  }

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

    // Thay vì gọi auction.start(), ta thay đổi trạng thái trực tiếp
    if (auction.getStatus() == AuctionStatus.WAITING) {
      auction.setStatus(AuctionStatus.ACTIVE);
      System.out.println("Auction " + auctionId + " is now ACTIVE.");
    }
  }

  // ========================
  // Place Bid
  // ========================
  public boolean placeBid(String auctionId,
                          String bidderId,
                          BigDecimal amount) {

    Auction auction = auctions.get(auctionId);
    if (auction == null) return false;

    // Sử dụng hàm applyBid có sẵn logic kiểm tra của bạn
    return auction.applyBid(bidderId, amount);
  }

  // ========================
  // Cancel Auction
  // ========================
  public void cancelAuction(String auctionId) {
    Auction auction = auctions.get(auctionId);
    if (auction == null) return;

    // Sử dụng hàm cancel có sẵn trong Auction.java của bạn
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
      // Kiểm tra nếu đã quá giờ kết thúc
      if (auction.getStatus() == AuctionStatus.ACTIVE &&
              now.isAfter(auction.getEndTime())) {

        // Cập nhật trạng thái sang FINISHED
        auction.setStatus(AuctionStatus.FINISHED);
        System.out.println("Auction closed: " + auction.getId());
      }
    }
  }
}