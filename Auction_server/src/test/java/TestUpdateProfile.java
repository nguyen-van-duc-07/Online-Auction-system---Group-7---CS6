package debug;

import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.User;
import java.time.LocalDate;
import repository.UserRepository;

/**
 * File test update profile.
 */
public class TestUpdateProfile {

  public static void main(String[] args) {

    // ID user muốn update
    String userId = "03ed5e0c-cdc9-4e68-af3c-035cab1cd807";

    // Tạo user test
    User user = new Bidder();

    user.setId(userId);

    user.setRealName("Nguyen Van A");

    user.setDob(
        LocalDate.of(2005, 8, 4)
    );

    user.setEmail("test@gmail.com");

    user.setPhoneNumber("0123456789");

    user.setAddress("Ha Noi");

    // Repository
    UserRepository userRepo = new UserRepository();

    // Update
    boolean updated =
        userRepo.updateProfile(user);

    // Result
    if (updated) {
      System.out.println(">>> Update profile SUCCESS!");
    } else {
      System.out.println(">>> Update profile FAILED!");
    }
  }
}