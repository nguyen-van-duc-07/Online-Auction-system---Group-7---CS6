package com.auction.shared.request;

import com.auction.shared.model.user.InfoDTO;
import lombok.Getter;

@Getter
public class ConfirmOrderRequestDTO implements RequestDTO {
  private String orderId;
  private InfoDTO buyerInfo;

  public ConfirmOrderRequestDTO(String orderId, InfoDTO buyerInfo) {
    this.orderId = orderId;
    this.buyerInfo = buyerInfo;
  }

}