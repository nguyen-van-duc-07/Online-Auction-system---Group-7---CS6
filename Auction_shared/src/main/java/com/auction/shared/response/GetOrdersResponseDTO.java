package com.auction.shared.response;

import com.auction.shared.model.order.OrderDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class GetOrdersResponseDTO implements ResponseDTO {
  private String message;
  private boolean success;
  private List<OrderDTO> orders;

  public GetOrdersResponseDTO(
      String message, boolean success, List<OrderDTO> completedOrders) {
    this.message = message;
    this.success = success;
    this.orders = completedOrders;
  }
}
