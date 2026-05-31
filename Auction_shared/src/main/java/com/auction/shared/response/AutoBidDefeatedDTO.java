package com.auction.shared.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class AutoBidDefeatedDTO implements ResponseDTO {
    private String auctionId;
    private String message;
}