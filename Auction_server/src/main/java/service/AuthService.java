package service;

import org.mindrot.jbcrypt.BCrypt;
import repository.UserRepository;

public class AuthService {

  private UserRepository repo = new UserRepository();

  public boolean login(String accountName, String password) {
    String hashedPassword = repo.getPasswordByAccountName(accountName);

    if (hashedPassword == null) return false;

    return BCrypt.checkpw(password, hashedPassword);
  }
}