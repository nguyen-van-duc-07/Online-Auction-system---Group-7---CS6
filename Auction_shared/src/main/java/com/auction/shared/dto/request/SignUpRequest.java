package com.auction.shared.dto.request;

import java.io.Serializable;

/**
 * Lớp DTO (Data Transfer Object) đại diện cho yêu cầu đăng ký tài khoản.
 * Đối tượng này đóng gói thông tin đăng ký (bao gồm tên tài khoản và mật khẩu)
 * được gửi từ phía Client lên Server. Lớp triển khai {@link Serializable}
 * để có thể truyền tải dữ liệu qua môi trường mạng (Network stream).
 */
public class SignUpRequest implements Serializable {

  private String accountName;
  private String password;

  public SignUpRequest() {
  }

  public SignUpRequest(String accountName, String password) {
    this.accountName = accountName;
    this.password = password;
  }

  public String getAccountName() {
    return accountName;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}