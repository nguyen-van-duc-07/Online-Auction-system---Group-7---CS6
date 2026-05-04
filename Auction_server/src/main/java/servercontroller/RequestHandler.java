package servercontroller;

import com.auction.shared.model.user.User;
import service.AuthService;

public class RequestHandler {
  public static String login(User loginUser) {
    boolean isSuccess = AuthService.login(loginUser.getUserName(), loginUser.getPassword());
    if (isSuccess) {
      return "LOGIN_SUCCESS";
    }
    return "LOGIN_FAILED";
  }

  public static String signup(User signupUser) {
    boolean isSuccess = AuthService.signup(signupUser);
    if (isSuccess) {
      return "SIGNUP_SUCCESS";
    }
    return "SIGNUP_FAILED";
  }
}
