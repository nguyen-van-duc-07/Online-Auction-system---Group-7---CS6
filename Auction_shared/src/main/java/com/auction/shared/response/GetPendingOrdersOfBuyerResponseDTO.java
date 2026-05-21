package com.auction.shared.response;

import com.auction.shared.model.order.OrderDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class GetPendingOrdersOfBuyerResponseDTO implements ResponseDTO {
  private String message;
  private boolean success;
  private List<OrderDTO> pendingOrders;

  public GetPendingOrdersOfBuyerResponseDTO(
      String message, boolean success, List<OrderDTO> pendingOrders) {
    this.message = message;
    this.success = success;
    this.pendingOrders = pendingOrders;
  }
}
