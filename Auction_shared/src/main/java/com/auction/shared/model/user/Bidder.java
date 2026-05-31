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

  public Bidder(UserDTO dto) {
    super(dto); // Gọi constructor của cha để gán các thuộc tính chung
  }

  @Override
  public String getDefaultAccountName() {
    return "user" + this.id.substring(0,6);
  }
}
