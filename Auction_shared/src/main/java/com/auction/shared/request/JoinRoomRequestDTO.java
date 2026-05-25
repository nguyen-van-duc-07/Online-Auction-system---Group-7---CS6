package com.auction.shared.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JoinRoomRequestDTO implements RequestDTO {
  private static final long serialVersionUID = 1L;

  /**
   * Biến lưu giá trị id của phiên đấu giá mà Client chọn.
   */
  private String selectedAuctionId;
}
