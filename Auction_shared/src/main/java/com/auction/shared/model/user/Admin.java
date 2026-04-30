package com.auction.shared.model.user;

import com.auction.shared.enums.UserRole;

import java.time.LocalDate;

public class Admin extends User {
  public Admin() {
  }

  public Admin(String userName, String password, String email, LocalDate dob, String phoneNumber, String address, UserRole role) {
    super(userName, password, email, dob, phoneNumber, address, role);
  }
}