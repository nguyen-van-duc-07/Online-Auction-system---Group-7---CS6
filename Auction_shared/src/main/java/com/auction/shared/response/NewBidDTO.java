package com.auction.shared.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NewBidDTO implements ResponseDTO {
  private String auctionId;
  private String bidderId;
  private String bidderName;
  private BigDecimal bidAmount;
}
