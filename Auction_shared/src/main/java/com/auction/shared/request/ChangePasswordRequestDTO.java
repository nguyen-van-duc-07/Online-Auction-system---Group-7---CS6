package com.auction.shared.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordRequestDTO implements RequestDTO {
  private String userId;
  private String oldPassword;
  private String newPassword;

  public ChangePasswordRequestDTO(String userId, String oldPassword, String newPassword) {
    this.userId = userId;
    this.oldPassword = oldPassword;
    this.newPassword = newPassword;
  }
}
