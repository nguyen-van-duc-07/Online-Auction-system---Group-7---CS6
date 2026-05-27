package com.auction.shared.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
@NoArgsConstructor // Cần thiết nếu sau này dùng các thư viện Map dữ liệu
public class Admin extends User {
  private static final Logger log = LoggerFactory.getLogger(Admin.class);
  public Admin(UserDTO dto) {
    super(dto);
  }

  @Override
  public String getDefaultAccountName() {
    return "admin" + this.id.substring(0,6);
  }
}