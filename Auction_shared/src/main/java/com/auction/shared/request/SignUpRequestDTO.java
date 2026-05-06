package com.auction.shared.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor

/**
 * Lớp DTO (Data Transfer Object) đại diện cho yêu cầu đăng ký tài khoản.
 * Đối tượng này đóng gói thông tin đăng ký (bao gồm tên tài khoản và mật khẩu)
 * được gửi từ phía Client lên Server. Lớp triển khai {@link Serializable}
 * để có thể truyền tải dữ liệu qua môi trường mạng (Network stream).
 */
public class SignUpRequestDTO implements RequestDTO {
  private String accountName;
  private String password;

  public SignUpRequestDTO(String accountName, String password) {
    this.accountName = accountName;
    this.password = password;
  }
}