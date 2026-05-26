package com.auction.shared.request;

import com.auction.shared.enums.SellerRegisterStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSellerProfileStatusRequestDTO implements RequestDTO {
  private String userId;
  private SellerRegisterStatus newStatus;
  private SellerRegisterStatus expectedOldStatus;
}
