package com.auction.shared.model.user;

import com.auction.shared.enums.UserRole;
import com.auction.shared.model.core.Entity;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@NoArgsConstructor

abstract public class User extends Entity {
    protected String userName;
    protected String password;
    protected String email;
    protected LocalDate dob;
    protected String phoneNumber;
    protected UserRole role;

    public User(UserDTO dto) {
        super();
        this.userName = dto.getUserName();
        this.password = dto.getPassword();
        this.email = dto.getEmail();
        this.dob = dto.getDob();
        this.phoneNumber = dto.getPhoneNumber();
        this.role = dto.getRole();
    }
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }
}
