package service;
import config.DatabaseConnection;

import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;

/**
 * Class xử lý các nghiệp vụ liên quan đến Ví tiền của người dùng.
 */
public class WalletService {

    /**
     * Lấy số dư hiện tại của người dùng.
     * Trả về BigDecimal để đảm bảo độ chính xác của tiền tệ.
     * * @param userId Mã định danh của người dùng
     * @return Số dư hiện tại, trả về null hoặc ném lỗi nếu không tìm thấy
     */
    public BigDecimal getBalance(String userId) throws Exception {
        // =========================================================
        String query = "SELECT balance FROM wallets WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Lấy ra dạng BigDecimal trực tiếp từ DB
                    return rs.getBigDecimal("balance");
                } else {
                    throw new Exception("Không tìm thấy ví của người dùng: " + userId);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi truy vấn số dư: " + e.getMessage());
            throw e; // Ném lỗi lên trên cho Controller xử lý (hiển thị thông báo)
        }
    }
}