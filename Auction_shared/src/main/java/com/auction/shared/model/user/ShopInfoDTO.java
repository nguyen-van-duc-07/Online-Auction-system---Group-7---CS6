package com.auction.shared.model.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShopInfoDTO {
  private String brandName;
  private String location;

  public ShopInfoDTO(String brandName, String location) {
    this.brandName = brandName;
    this.location = location;
  }
}
