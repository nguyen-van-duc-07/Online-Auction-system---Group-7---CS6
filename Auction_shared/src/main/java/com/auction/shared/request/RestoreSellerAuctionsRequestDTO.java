package com.auction.shared.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestoreSellerAuctionsRequestDTO implements RequestDTO {
  private String userId;
}
