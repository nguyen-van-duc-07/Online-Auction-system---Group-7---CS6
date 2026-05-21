package com.auction.shared.request;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
@Getter
@AllArgsConstructor
public class SetAutoBidRequestDTO implements RequestDTO {
  private static final long serialVersionUID = 1L;
  private String userId;
  private String auctionId;
  private BigDecimal maxPrice;
  private BigDecimal stepAmount;
  private boolean isActive;
}