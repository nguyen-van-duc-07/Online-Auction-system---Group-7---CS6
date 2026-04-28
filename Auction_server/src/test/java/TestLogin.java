import service.AuthService;

public class TestLogin {
    public static void main(String[] args) {
        AuthService auth = new AuthService();

        String account_name = "admin";
        String password = "123456"; // ✅ plain text

        boolean ok = auth.login(account_name, password);

        if (ok) {
            System.out.println("✅ LOGIN OK");
        } else {
            System.out.println("❌ LOGIN FAIL");
        }
    }
}