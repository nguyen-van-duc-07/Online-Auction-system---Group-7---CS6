package com.auction.shared.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Lớp DTO mang yêu cầu cập nhật thông tin cá nhân của tài khoản đang trong phiên gửi lên Server.
 *
 * <p>Chứa toàn bộ thông tin để Server thực hiện việc cập nhật thông tin cho tài khoản.</p>
 */
@Getter
@AllArgsConstructor
public class UpdateProfileRequestDTO implements RequestDTO {
  /**
   * Lưu thông tin id của tài khoản hiện tại.
   */
  private String userId;
  /**
   * Lưu thông tin tên thật của chủ tài khoản.
   */
  private String realName;
  /**
   * Lưu thông tin email của chủ tài khoản.
   */
  private String email;
  /**
   * Lưu thông tin ngày sinh của chủ tài khoản.
   */
  private LocalDate birthDate;
  /**
   * Lưu thông tin địa chỉ của chủ tài khoản.
   */
  private String address;
}
