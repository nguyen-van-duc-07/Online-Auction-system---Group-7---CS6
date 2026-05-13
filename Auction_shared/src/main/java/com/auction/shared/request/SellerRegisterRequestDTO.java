package com.auction.shared.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerRegisterRequestDTO implements RequestDTO{
  private String userId;
  private String brandName;
  private String citizenIdentityCard;
  private String location;
  private String bankAccount;
  private String bankName;

  public SellerRegisterRequestDTO(String userId, String brandName, String citizenIdentityCard,
                                  String location, String bankAccount, String bankName) {
    this.userId = userId;
    this.brandName = brandName;
    this.citizenIdentityCard = citizenIdentityCard;
    this.location = location;
    this.bankAccount = bankAccount;
    this.bankName = bankName;
  }
}
