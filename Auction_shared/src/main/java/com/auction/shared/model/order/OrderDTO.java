package com.auction.shared.model.order;

import com.auction.shared.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class OrderDTO implements Serializable {
  private String orderId;
  private String auctionId;
  private String brandName;
  private String itemName;
  private BigDecimal finalPrice;
  private OrderStatus status;
  private String winnerName;
}
