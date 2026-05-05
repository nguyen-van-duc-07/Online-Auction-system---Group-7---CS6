package com.auction.shared.model.user;

import com.auction.shared.enums.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Admin extends User {
  public Admin() {
  }

  public Admin(String id, LocalDateTime createdAt, String userName, String password) {
    super(id, createdAt, userName, password);
  }

  public Admin(String userName, String password, String email, LocalDate dob, String phoneNumber, String address, UserRole role) {
    super(userName, password, email, dob, phoneNumber, address, role);
  }
}