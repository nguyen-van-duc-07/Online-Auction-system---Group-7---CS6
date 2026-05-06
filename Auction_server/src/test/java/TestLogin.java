import com.auction.shared.model.user.User;
import com.auction.shared.request.LoginRequestDTO;
import service.AuthService;

/**
 * Lớp kiểm thử (Test) độc lập dành cho tính năng Đăng nhập phía Server.
 * <p>
 * Lớp này giả lập một yêu cầu đăng nhập từ Client bằng cách tạo một {@link LoginRequestDTO},
 * sau đó gọi trực tiếp vào tầng Service để kiểm tra tính đúng đắn của logic
 * xác thực mật khẩu bằng BCrypt và truy vấn Database.
 * </p>
 */
public class TestLogin {
    /**
     * Phương thức chạy chính để thực thi kịch bản kiểm thử.
     *
     * @param args Các tham số truyền vào từ dòng lệnh (không sử dụng)
     */
    public static void main(String[] args) {
        // Giả lập một gói tin Request từ Client
        LoginRequestDTO loginUser = new LoginRequestDTO("admin", "123456");

        // Gọi trực tiếp hàm static từ AuthService để xác thực
        User loggedInUser = AuthService.login(loginUser);

        // Kiểm tra kết quả trả về và in ra Console
        if (loggedInUser != null) {
            System.out.println("  LOGIN OK - Đăng nhập thành công!");
            System.out.println("  > ID: " + loggedInUser.getId());
            System.out.println("  > Account: " + loggedInUser.getAccountName());
            System.out.println("  > Real Name: " + loggedInUser.getRealName());
        } else {
            System.out.println("  LOGIN FAIL - Sai tài khoản hoặc mật khẩu!");
        }
    }
}