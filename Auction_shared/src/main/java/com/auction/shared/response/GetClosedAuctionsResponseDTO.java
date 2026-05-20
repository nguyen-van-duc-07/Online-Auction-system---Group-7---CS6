package com.auction.shared.response;

import java.util.List;

import com.auction.shared.model.auction.AuctionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response chứa danh sách các phiên đấu giá đã kết thúc.
 */
@Getter
@AllArgsConstructor
public class GetClosedAuctionsResponseDTO implements ResponseDTO {
  private boolean success;
  private String message;
  private List<AuctionDTO> closedAuctions;
}
