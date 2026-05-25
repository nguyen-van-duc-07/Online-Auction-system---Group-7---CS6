package com.auction.shared.model.user;

import com.auction.shared.model.auction.Auction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor

public class Bidder extends User{
  private Wallet wallet;
  private SellerProfile sellerProfile;
  private List<String> joinedAuctionIds = new ArrayList<>(); // Lưu danh sách ID các phiên đã tham gia

  public Bidder(UserDTO dto) {
    super(dto); // Gọi constructor của cha để gán các thuộc tính chung
    this.joinedAuctionIds = new ArrayList<>();
  }

  public void openSellerProfile() {
    if (this.sellerProfile != null) {
      throw new IllegalStateException("Tài khoản này đã là Người bán rồi!");
    }
    this.sellerProfile = new SellerProfile(this.id);
  }
  
  public void bid(Auction auction, BigDecimal amount){
    if (this.joinedAuctionIds == null) {
      this.joinedAuctionIds = new ArrayList<>();
    }

    // Xác định số tiền đã đặt trước đó trong phiên này (nếu đang là người giữ giá cao nhất)
    BigDecimal previousAmount = BigDecimal.ZERO;
    if (this.id != null && this.id.equals(auction.getHighestBidderId())) {
      previousAmount = auction.getCurrentHighestPrice();
    }

    BigDecimal incrementalAmount = amount.subtract(previousAmount);

    if (incrementalAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Giá thầu mới phải lớn hơn giá thầu cũ!");
    }

    // 1. Kiểm tra tiền trước khi freeze
    if (this.wallet.getBalance().compareTo(incrementalAmount) < 0) {
      throw new IllegalArgumentException("Số dư không đủ để thực hiện trả giá!");
    }

    // 2. Đóng băng tiền
    this.wallet.freeze(incrementalAmount);

    // 3. Thêm vào danh sách tham gia nếu chưa có
    if (!joinedAuctionIds.contains(auction.getId())) {
      joinedAuctionIds.add(auction.getId());
    }
  }
  @Override
  public String getDefaultAccountName() {
    return "user" + this.id.substring(0,6);
  }
}
