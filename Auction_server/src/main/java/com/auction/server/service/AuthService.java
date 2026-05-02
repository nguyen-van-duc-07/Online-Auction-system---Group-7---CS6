package com.auction.server.service;

import org.mindrot.jbcrypt.BCrypt;
import com.auction.server.repository.UserRepository;

public class AuthService {

  private UserRepository repo = new UserRepository();

  public boolean login(String accountName, String password) {
    String hashedPassword = repo.getPasswordByAccountName(accountName);

    if (hashedPassword == null) return false;

    return BCrypt.checkpw(password, hashedPassword);
  }

  public boolean register(String username, String password, String fullName) {
    // Mã hóa mật khẩu trước khi lưu
    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

    // Mặc định cho người dùng mới 1,000,000đ để đấu giá nhé
    return repo.saveUser(username, hashedPassword, fullName, 1000000.0);
  }
}