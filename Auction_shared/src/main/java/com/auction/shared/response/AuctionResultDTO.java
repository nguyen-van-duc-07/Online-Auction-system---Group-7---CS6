package com.auction.shared.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
public class AuctionResultDTO implements ResponseDTO {
  private String auctionId;
  private String winnerId;
  private String itemName;
  private BigDecimal finalPrice;
}