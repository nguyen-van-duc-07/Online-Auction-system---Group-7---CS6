package com.auction.shared.response;

import java.util.List;

import com.auction.shared.model.auction.AuctionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response chứa danh sách các phiên đấu giá đang chờ bắt đầu.
 */
@Getter
@AllArgsConstructor
public class GetWaitingAuctionsResponseDTO implements ResponseDTO {
  private boolean success;
  private String message;
  private List<AuctionDTO> waitingAuctions;
}
