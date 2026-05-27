package com.auction.shared.request;

import com.auction.shared.enums.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAuctionStatusRequestDTO implements RequestDTO {
  private String auctionId;
  private AuctionStatus status;
}
