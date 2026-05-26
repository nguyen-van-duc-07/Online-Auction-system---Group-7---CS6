package com.auction.shared.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSellerProfileStatusResponseDTO implements ResponseDTO {
  private boolean success;
  private String message;
}
