package com.auction.shared.model.auction;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.core.Entity;
import com.auction.shared.model.item.Item;
import com.auction.shared.model.transaction.BidTransaction;
import jakarta.persistence.*; // Dùng Jakarta cho Spring Boot 3
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor // Cần thiết cho JPA
@jakarta.persistence.Entity
@Table(name = "auctions")
public class Auction extends Entity implements Serializable{

    @OneToOne // Giả định mỗi phiên đấu giá cho 1 món đồ
    @JoinColumn(name = "item_id")
    private Item item;

    private BigDecimal startPrice;
    private BigDecimal currentHighestPrice;
    private String highestBidderId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @Version // Khóa lạc quan (Optimistic Locking) để tránh lỗi khi nhiều người cùng bid
    private Long version;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private List<BidTransaction> bidHistory = new ArrayList<>();

    // Constructor chính
    public Auction(Item item, BigDecimal startPrice, LocalDateTime startTime, LocalDateTime endTime) {
        this.item = item;
        this.startPrice = startPrice;
        this.currentHighestPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AuctionStatus.WAITING;
    }

    // Logic nghiệp vụ đặt tại Entity (Domain Logic)
    public boolean applyBid(String bidderId, BigDecimal bidAmount) {
        if (this.status != AuctionStatus.ACTIVE) {
            return false;
        }

        if (bidAmount.compareTo(this.currentHighestPrice) <= 0) {
            return false;
        }

        this.currentHighestPrice = bidAmount;
        this.highestBidderId = bidderId;

        BidTransaction newTransaction = new BidTransaction(bidderId, this.getId(), bidAmount);
        this.bidHistory.add(newTransaction);

        return true;
    }

    public void cancel() {
        if (this.status == AuctionStatus.CANCELED) {
            throw new IllegalStateException("Auction already cancelled");
        }
        this.status = AuctionStatus.CANCELED;
    }
}