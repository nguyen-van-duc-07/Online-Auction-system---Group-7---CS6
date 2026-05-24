package com.auction.shared.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateAdminRequestDTO implements RequestDTO {
  private String accountName;
  private String password;
  private LocalDate dob;
  private String email;
  private String phoneNumber;
  private String address;
}
