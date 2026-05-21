package com.auction.shared.model.order;

import com.auction.shared.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class OrderDTO implements Serializable {
  private String orderId;
  private String brandName;
  private String itemName;
  private LocalDate startTime;
  private LocalDate endTime;
  private BigDecimal finalPrice;
  private OrderStatus status;
  private String winner;
}
