package com.auction.shared.response;

import com.auction.shared.model.order.OrderDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class GetCancelledOrdersOfSellerResponseDTO implements ResponseDTO {
  private String message;
  private boolean success;
  private List<OrderDTO> cancelledOrders;

  public GetCancelledOrdersOfSellerResponseDTO(
      String message, boolean success, List<OrderDTO> cancelledOrders) {
    this.message = message;
    this.success = success;
    this.cancelledOrders = cancelledOrders;
  }
}
