package com.auction.shared.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JoinRoomResponseDTO implements ResponseDTO {
  private boolean success;
  private String message;
  private AuctionResponseDTO auction;
}
