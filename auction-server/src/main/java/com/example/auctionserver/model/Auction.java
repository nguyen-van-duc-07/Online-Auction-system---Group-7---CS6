package com.example.auctionserver.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Auction extends Entity {
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
        if (this.status != AuctionStatus.ACTIVE) {
            System.out.println("Phiên đấu giá chưa mở hoặc đã kết thúc!");
            return false;
        }

        // So sánh: bidAmount <= currentHighestPrice
        // compareTo trả về -1 (nếu nhỏ hơn), 0 (nếu bằng), 1 (nếu lớn hơn)
        if (bidAmount.compareTo(this.currentHighestPrice) <= 0) {
            System.out.println("Giá đặt phải lớn hơn giá hiện tại!");
            return false;
        }

        // Cập nhật giá và người dẫn đầu
        this.currentHighestPrice = bidAmount;
        this.highestBidderId = bidderId;

        // Tạo Transaction rất gọn gàng vì tất cả đều đã là BigDecimal
        BidTransaction newTransaction = new BidTransaction(bidderId, this.getId(), bidAmount);
        this.bidHistory.add(newTransaction);

        return true;
    }

    public void cancel() {
        if (this.status == AuctionStatus.CANCELED) {
            throw new IllegalStateException("This auction was already cancelled");
        }
        this.status = AuctionStatus.CANCELED;
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