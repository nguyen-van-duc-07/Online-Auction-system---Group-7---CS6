package com.auction.shared.response;

import com.auction.shared.model.order.Order;

public class GetOrderResponseDTO implements ResponseDTO {
  private boolean success;
  private String message;
  private Order order;
  private String itemName;
  private String itemId;

  public GetOrderResponseDTO(boolean success, String message, Order order) {
    this.success = success;
    this.message = message;
    this.order   = order;
  }

  public GetOrderResponseDTO(boolean success, String message, Order order, String itemName, String itemId) {
    this.success = success;
    this.message = message;
    this.order   = order;
    this.itemName = itemName;
    this.itemId = itemId;
  }

  public boolean isSuccess() { return success; }
  public String getMessage() { return message; }
  public Order getOrder()    { return order; }
  public String getItemName() { return itemName; }
  public String getItemId()   { return itemId; }
}