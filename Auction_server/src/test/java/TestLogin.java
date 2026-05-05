import com.auction.shared.request.LoginRequestDTO;
import service.AuthService;

public class TestLogin {
    public static void main(String[] args) {
        AuthService auth = new AuthService();

        LoginRequestDTO loginUser = new LoginRequestDTO("admin", "123456");

        boolean ok = auth.login(loginUser);

        if (ok) {
            System.out.println("✅ LOGIN OK");
        } else {
            System.out.println("❌ LOGIN FAIL");
        }
    }
}