package com.auction.shared.model.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
@NoArgsConstructor
@Getter
@Setter
public class InfoDTO implements Serializable {
  private String consigneeName;
  private String phoneNumber;
  private String address;

  public InfoDTO(String consigneeName, String phoneNumber, String address) {
    this.consigneeName = consigneeName;
    this.phoneNumber = phoneNumber;
    this.address = address;
  }
}
