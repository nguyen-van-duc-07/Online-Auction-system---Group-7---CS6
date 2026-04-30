package com.auction.shared.model.user;

import com.auction.shared.enums.UserRole;
import com.auction.shared.model.core.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

abstract public class User extends Entity {
  protected String userName;
  protected String password;
  protected String email;
  protected LocalDate dob;
  protected String phoneNumber;
  protected String address;
  protected UserRole role;

  public User() {
  }

  public User(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }

  public User(String id, LocalDateTime createdAt, String userName, String password) {
    super(id, createdAt);
    this.userName = userName;
    this.password = password;
  }

  public User(String userName, String password, String email, LocalDate dob, String phoneNumber, String address, UserRole role) {
    this.userName = userName;
    this.password = password;
    this.email = email;
    this.dob = dob;
    this.phoneNumber = phoneNumber;
    this.address = address;
    this.role = role;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public LocalDate getDob() {
    return dob;
  }

  public void setDob(LocalDate dob) {
    this.dob = dob;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public UserRole getRole() {
    return role;
  }

}
