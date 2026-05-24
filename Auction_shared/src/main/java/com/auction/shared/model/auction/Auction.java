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
public class Auction extends Entity implements Serializable {

  @OneToOne // Giả định mỗi phiên đấu giá cho 1 món đồ
  @JoinColumn(name = "item_id")
  private Item item;

  private String sellerId;
  private BigDecimal startPrice;
  private BigDecimal minStepPrice;
  private BigDecimal currentHighestPrice;
  private String highestBidderId;
  private String highestBidderName;

  private LocalDateTime startTime;
  private LocalDateTime endTime;

  @Enumerated(EnumType.STRING)
  private AuctionStatus status;

  @Version // Khóa lạc quan (Optimistic Locking) để tránh lỗi khi nhiều người cùng bid
  private Long version;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "auction_id")
  private List<BidTransaction> bidHistory = new ArrayList<>();

  public Auction(Item item, String sellerId, BigDecimal startPrice, BigDecimal minStepPrice, LocalDateTime startTime, LocalDateTime endTime) {
    this.item = item;
    this.sellerId = sellerId;
    this.startPrice = startPrice;
    this.minStepPrice = minStepPrice;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  // Constructor chính
  public Auction(Item item, BigDecimal startPrice, LocalDateTime startTime, LocalDateTime endTime) {
    this.item = item;
    this.startPrice = startPrice;
    this.currentHighestPrice = startPrice;
    this.startTime = startTime;
    this.endTime = endTime;
    // Nếu thời gian bắt đầu bằng hoặc trước thời điểm hiện tại -> ACTIVE luôn
    // Ngược lại (hẹn giờ trong tương lai) -> WAITING
    if (this.startTime == null || !this.startTime.isAfter(LocalDateTime.now())) {
      this.status = AuctionStatus.ACTIVE;
    } else {
      this.status = AuctionStatus.WAITING;
    }
  }

  public Auction(Item item, BigDecimal startPrice, BigDecimal minStepPrice, LocalDateTime startTime, LocalDateTime endTime) {
    this.item = item;
    this.startPrice = startPrice;
    this.minStepPrice = minStepPrice;
    this.currentHighestPrice = startPrice;
    this.startTime = startTime;
    this.endTime = endTime;
    // Nếu thời gian bắt đầu bằng hoặc trước thời điểm hiện tại -> ACTIVE luôn
    // Ngược lại (hẹn giờ trong tương lai) -> WAITING
    if (this.startTime == null || !this.startTime.isAfter(LocalDateTime.now())) {
      this.status = AuctionStatus.ACTIVE;
    } else {
      this.status = AuctionStatus.WAITING;
    }
  }

  // --- CÁC PHƯƠNG THỨC MỚI ĐƯỢC BỔ SUNG ---

  /**
   * Bắt đầu phiên đấu giá (chuyển trạng thái từ WAITING sang ACTIVE).
   */
  public void start() {
    if (this.status != AuctionStatus.WAITING) {
      return; // Chỉ cho phép start khi đang ở trạng thái chờ
    }
    this.status = AuctionStatus.ACTIVE;
  }

  /**
   * Đóng phiên đấu giá (chuyển trạng thái từ ACTIVE sang FINISHED).
   */
  public void close() {
    if (this.status != AuctionStatus.ACTIVE) {
      return; // Chỉ đóng được những phiên đang chạy
    }
    this.status = AuctionStatus.CLOSED;
  }

  /**
   * Kiểm tra xem phiên đấu giá đã hết hạn (quá endTime) hay chưa.
   */
  public boolean isExpired() {
    // Nếu endTime bị null (chưa set) thì mặc định chưa hết hạn
    if (this.endTime == null) return false;

    // Nếu thời gian hiện tại đã vượt qua endTime
    return LocalDateTime.now().isAfter(this.endTime);
  }

  // --- KẾT THÚC PHẦN BỔ SUNG ---

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

    BidTransaction newTransaction = new BidTransaction(this.getId(), bidderId, bidAmount);
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