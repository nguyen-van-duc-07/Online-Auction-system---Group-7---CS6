package com.auction.shared.response;

import com.auction.shared.model.auction.AuctionDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class GetActiveAuctionsBySellerResponseDTO implements ResponseDTO {
  private String message;
  private boolean success;
  private List<AuctionDTO> activeAuctionsBelongToSeller;

  public GetActiveAuctionsBySellerResponseDTO(boolean success, String message,
                                              List<AuctionDTO> activeAuctionsBelongToSeller) {
    this.success = success;
    this.message = message;
    this.activeAuctionsBelongToSeller = activeAuctionsBelongToSeller;
  }
}
