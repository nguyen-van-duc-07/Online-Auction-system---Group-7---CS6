package com.auction.shared.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SellerRegisterRequestDTO implements RequestDTO {
  private String userId;
  private String brandName;
  private String citizenIdentityCard;
  private String location;
  private String bankAccount;
  private String bankName;
  private String status;
  private LocalDateTime createdAt;
}
