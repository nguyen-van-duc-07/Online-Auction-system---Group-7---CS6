package com.auction.shared.model.user;
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
  private List<String> joinedAuctionIds; // Lưu danh sách ID các phiên đã tham gia

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
  public void bid(String auctionId, BigDecimal amount){
    // 1. Kiểm tra tiền trước khi freeze
    if (this.wallet.getBalance().compareTo(amount) < 0) {
      throw new IllegalArgumentException("Số dư không đủ để thực hiện trả giá!");
    }
    // 2. Đóng băng tiền
    this.wallet.freeze(amount);
    // 3. Thêm vào danh sách tham gia nếu chưa có
    if (!joinedAuctionIds.contains(auctionId)) {
      joinedAuctionIds.add(auctionId);
    }
  }
}
