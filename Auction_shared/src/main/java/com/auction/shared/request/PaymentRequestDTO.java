package com.auction.shared.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@ToString           // Phục vụ in Log rõ ràng khi gửi/nhận qua Socket
@EqualsAndHashCode  // Phục vụ viết JUnit Test
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String auctionId;
    private String itemId;
    private BigDecimal amount;
    private String paymentMethod;
}