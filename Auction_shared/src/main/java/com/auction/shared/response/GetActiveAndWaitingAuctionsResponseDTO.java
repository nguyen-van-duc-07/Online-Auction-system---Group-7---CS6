package com.auction.shared.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GetActiveAndWaitingAuctionsResponseDTO implements ResponseDTO {
  private String message;
  private boolean success;
  private List<AuctionResponseDTO> activeAndWaitingAuctions;

  public GetActiveAndWaitingAuctionsResponseDTO(boolean success, String message, List<AuctionResponseDTO> activeAndWaitingAuctions) {
    this.success = success;
    this.message = message;
    this.activeAndWaitingAuctions = activeAndWaitingAuctions;
  }
}
