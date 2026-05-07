package com.auction.shared.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionRequestDTO {
    @NotBlank
    private String itemId;
    @Positive
    private BigDecimal startPrice;
    @Future
    private LocalDateTime startTime;
    @Future
    private LocalDateTime endTime;
}
