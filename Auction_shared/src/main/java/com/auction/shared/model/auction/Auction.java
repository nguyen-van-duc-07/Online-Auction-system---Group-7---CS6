package com.auction.shared.model.auction;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.core.Entity;
import com.auction.shared.model.item.Item;
import com.auction.shared.model.transaction.BidTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

public class Auction extends Entity {
  private final ReentrantLock lock = new ReentrantLock();
  private Item item;

  // Đã đổi sang BigDecimal
  private BigDecimal startPrice;
  private BigDecimal currentHighestPrice;

  private String highestBidderId;
  private LocalDateTime startTime, endTime;
  private AuctionStatus status;
  private PriorityQueue<AutoBidConfig> autoBidQueue;
  private List<BidTransaction> bidHistory;

  // Constructor nhận tham số BigDecimal
  public Auction(Item item, BigDecimal startPrice, LocalDateTime startTime, LocalDateTime endTime) {
    super();
    this.item = item;
    this.startPrice = startPrice;
    this.currentHighestPrice = startPrice; // Giá hiện tại = khởi điểm
    this.startTime = startTime;
    this.endTime = endTime;
    this.status = AuctionStatus.WAITING;

    this.autoBidQueue = new PriorityQueue<>();
    this.bidHistory = new ArrayList<>();
  }

  // Hàm applyBid nhận tham số BigDecimal
  public boolean applyBid(String bidderId, BigDecimal bidAmount) {

    lock.lock();
    try {

      if (status != AuctionStatus.ACTIVE) return false;

      if (LocalDateTime.now().isAfter(endTime)) {
        status = AuctionStatus.FINISHED;
        return false;
      }

      if (bidAmount.compareTo(currentHighestPrice) <= 0) return false;

      currentHighestPrice = bidAmount;
      highestBidderId = bidderId;

      bidHistory.add(new BidTransaction(bidderId, getId(), bidAmount));

      return true;

    } finally {
      lock.unlock();
    }
  }
  public boolean canStart() {
    return status == AuctionStatus.WAITING;
  }

  public boolean canBid() {
    return status == AuctionStatus.ACTIVE
        && LocalDateTime.now().isBefore(endTime);
  }

  public boolean canCancel() {
    return status == AuctionStatus.WAITING
        || status == AuctionStatus.ACTIVE;
  }

  public void start() {
    lock.lock();
    try {
      if (status != AuctionStatus.WAITING) return;
      status = AuctionStatus.ACTIVE;
    } finally {
      lock.unlock();
    }
  }

  public void close() {
    lock.lock();
    try {
      if (status != AuctionStatus.ACTIVE) return;
      status = AuctionStatus.FINISHED;
    } finally {
      lock.unlock();
    }
  }

  public void cancel() {
    lock.lock();
    try {
      if (status == AuctionStatus.FINISHED) return;
      status = AuctionStatus.CANCELED;
    } finally {
      lock.unlock();
    }
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(endTime);
  }

  // --- GETTERS / SETTERS ---

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public BigDecimal getStartPrice() {
    return startPrice;
  }

  public void setStartPrice(BigDecimal startPrice) {
    this.startPrice = startPrice;
  }

  public BigDecimal getCurrentHighestPrice() {
    return currentHighestPrice;
  }

  public void setCurrentHighestPrice(BigDecimal currentHighestPrice) {
    this.currentHighestPrice = currentHighestPrice;
  }

  public String getHighestBidderId() {
    return highestBidderId;
  }

  public void setHighestBidderId(String highestBidderId) {
    this.highestBidderId = highestBidderId;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalDateTime endTime) {
    this.endTime = endTime;
  }

  public AuctionStatus getStatus() {
    return status;
  }

  public void setStatus(AuctionStatus status) {
    this.status = status;
  }

  public PriorityQueue<AutoBidConfig> getAutoBidQueue() {
    return autoBidQueue;
  }

  public void setAutoBidQueue(PriorityQueue<AutoBidConfig> autoBidQueue) {
    this.autoBidQueue = autoBidQueue;
  }

  public List<BidTransaction> getBidHistory() {
    return bidHistory;
  }

  public void setBidHistory(List<BidTransaction> bidHistory) {
    this.bidHistory = bidHistory;
  }
}