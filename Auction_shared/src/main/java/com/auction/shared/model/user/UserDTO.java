package com.auction.shared.model.user;
import com.auction.shared.enums.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

@Getter    // Tự tạo tất cả Getter
@Setter
@SuperBuilder // Tự tạo toàn bộ logic Builder Pattern ở Cách 1 khi compile
@NoArgsConstructor
public class UserDTO implements Serializable {
    protected String accountName;
    protected String password;
    protected String email;
    protected LocalDate dob;
    protected String phoneNumber;
    protected UserRole role;
}