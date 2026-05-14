package com.auction.shared.request;

import com.auction.shared.enums.SellerRegisterStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateSellerProfileStatusRequestDTO implements RequestDTO {
  private String userId;
  private SellerRegisterStatus newStatus;
}
