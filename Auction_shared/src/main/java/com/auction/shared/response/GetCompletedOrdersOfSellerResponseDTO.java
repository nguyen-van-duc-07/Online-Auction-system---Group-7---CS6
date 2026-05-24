package com.auction.shared.response;

import com.auction.shared.model.order.OrderDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class GetCompletedOrdersOfSellerResponseDTO implements ResponseDTO {
  private String message;
  private boolean success;
  private List<OrderDTO> completedOrders;

  public GetCompletedOrdersOfSellerResponseDTO(
      String message, boolean success, List<OrderDTO> completedOrders) {
    this.message = message;
    this.success = success;
    this.completedOrders = completedOrders;
  }
}
