package com.auction.shared.request;
import lombok.Getter;

import java.math.BigDecimal;
@Getter
public class SetAutoBidRequestDTO implements RequestDTO {
  private String userId;
  private String auctionId;
  private BigDecimal maxPrice;
  private BigDecimal stepAmount;

  public SetAutoBidRequestDTO(String userId, String auctionId,
                              BigDecimal maxPrice, BigDecimal stepAmount) {
    this.userId    = userId;
    this.auctionId = auctionId;
    this.maxPrice  = maxPrice;
    this.stepAmount = stepAmount;
  }


}