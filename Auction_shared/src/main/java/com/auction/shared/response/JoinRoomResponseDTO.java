package com.auction.shared.response;

import com.auction.shared.model.auction.AutoBidConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JoinRoomResponseDTO implements ResponseDTO {
  private static final long serialVersionUID = 1L;

  private boolean success;
  private String message;
  private AuctionResponseDTO auction;
  private AutoBidConfig autoBidConfig;
}
