package com.auction.shared.model.user;

import com.auction.shared.enums.UserRole;
import com.auction.shared.model.core.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@NoArgsConstructor
@Getter
@Setter

abstract public class User extends Entity {
  protected String accountName;
  protected String password;
  protected String realName;
  protected LocalDate dob;
  protected String email;
  protected String phoneNumber;
  protected String address;
  protected UserRole role;

  public User(UserDTO dto) {
    super();
    this.realName = dto.getRealName();
    this.accountName = dto.getAccountName();
    this.password = dto.getPassword();
    this.email = dto.getEmail();
    this.dob = dto.getDob();
    this.phoneNumber = dto.getPhoneNumber();
    this.address = dto.getAddress();
    this.role = dto.getRole();
  }
  public boolean isAdmin() {
    return UserRole.ADMIN.equals(this.role);
  }
  public abstract String getDefaultAccountName();
}