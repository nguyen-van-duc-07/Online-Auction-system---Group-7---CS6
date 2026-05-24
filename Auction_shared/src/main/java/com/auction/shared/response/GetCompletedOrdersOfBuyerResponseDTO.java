package com.auction.shared.response;

import com.auction.shared.model.order.OrderDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class GetCompletedOrdersOfBuyerResponseDTO implements ResponseDTO {
  private String message;
  private boolean success;
  private List<OrderDTO> completedOrders;

  public GetCompletedOrdersOfBuyerResponseDTO(
      String message, boolean success, List<OrderDTO> completedOrders) {
    this.message = message;
    this.success = success;
    this.completedOrders = completedOrders;
  }
}
