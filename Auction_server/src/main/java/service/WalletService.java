package service;
import config.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;

import com.auction.shared.enums.WalletTransactionStatus;
import com.auction.shared.enums.WalletTransactionType;
import com.auction.shared.model.transaction.WalletTransaction;
import com.auction.shared.model.user.Wallet;
import repository.WalletRepository;
import repository.WalletTransactionRepository;
import repository.debug.Format;

import java.math.BigDecimal;
import java.sql.Connection;

public class WalletService {
    private final WalletRepository walletRepo = new WalletRepository();
    private final WalletTransactionRepository txRepo = new WalletTransactionRepository();
    /**
     * Lấy số dư hiện tại của người dùng.
     * Trả về BigDecimal để đảm bảo độ chính xác của tiền tệ.
     * @param userId Mã định danh của người dùng
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
    public void freezeMoney(Connection conn, String userId, BigDecimal amount, String auctionId) {
        System.out.println("[WALLET - FREEZE] Yêu cầu đóng băng " + amount + " của User: " + userId + " (Auction: " + auctionId + ")");
        Wallet wallet = walletRepo.getWalletByUserIdForUpdate(conn, userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Số dư không đủ để đặt giá");
        }
        // Lưu lại trạng thái trước khi thay đổi
        BigDecimal balBefore = wallet.getBalance();
        BigDecimal frozBefore = wallet.getFrozenBalance();
        // 1. Cập nhật ví
        wallet.setBalance(balBefore.subtract(amount));
        wallet.setFrozenBalance(frozBefore.add(amount));
        walletRepo.updateWallet(conn, wallet);
        // 2. Ghi nhận giao dịch
        WalletTransaction tx = new WalletTransaction(
                wallet.getId(),
                WalletTransactionType.BID_FREEZE,
                amount.negate(),
                balBefore,
                wallet.getBalance(),
                frozBefore,
                wallet.getFrozenBalance(),
                auctionId,
                WalletTransactionStatus.SUCCESS
        );
        txRepo.saveWalletTransaction(conn, tx);
        System.out.println("[WALLET - FREEZE] Thành công! User: " + userId + " | Số dư khả dụng: " + balBefore + " -> " + wallet.getBalance() + " | Đang đóng băng: " + frozBefore + " -> " + wallet.getFrozenBalance());
    }

    public void releaseFrozen(Connection conn, String userId, BigDecimal amount, String auctionId) {
        System.out.println("[WALLET - RELEASE] Yêu cầu hoàn trả " + amount + " cho User: " + userId + " (Auction: " + auctionId + ")");
        Wallet wallet = walletRepo.getWalletByUserIdForUpdate(conn, userId);

    BigDecimal balBefore = wallet.getBalance();
    BigDecimal frozBefore = wallet.getFrozenBalance();

    wallet.setFrozenBalance(frozBefore.subtract(amount));
    wallet.setBalance(balBefore.add(amount));
    walletRepo.updateWallet(conn, wallet);

    WalletTransaction tx = new WalletTransaction(
            wallet.getId(),
            WalletTransactionType.BID_RELEASE,
            amount,
            balBefore,
            wallet.getBalance(),
            frozBefore,
            wallet.getFrozenBalance(),
            auctionId,
            WalletTransactionStatus.SUCCESS
    );
    txRepo.saveWalletTransaction(conn, tx);
    System.out.println("[WALLET - RELEASE] Thành công! User: " + userId
            + " | Số dư: " + Format.fmt(balBefore) + " -> " + Format.fmt(wallet.getBalance())
            + " | Đóng băng: " + Format.fmt(frozBefore) + " -> " + Format.fmt(wallet.getFrozenBalance()));
    }

    /**
     * Hàm cập nhật số dư ví trực tiếp dưới Database
     */
    public boolean deposit(String userId, BigDecimal amount) {
        // Cập nhật đúng tên bảng là 'wallets' và cột khóa ngoại là 'user_id'
        String query = "UPDATE wallets SET balance = balance + ? WHERE user_id = ?";

        // Lấy connection từ class cấu hình DB của bạn
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Tham số 1: Số tiền cần nạp (thay vào dấu ? thứ nhất)
            pstmt.setBigDecimal(1, amount);

            // Tham số 2: ID của người dùng (thay vào dấu ? thứ hai)
            pstmt.setString(2, userId);

            // ExecuteUpdate trả về số dòng bị ảnh hưởng.
            int rowsAffected = pstmt.executeUpdate();

            // Nếu có ít nhất 1 dòng được update tức là nạp tiền thành công
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean withdraw(String userId, BigDecimal amount) {
        // Câu lệnh UPDATE kèm điều kiện balance >= ? để đảm bảo không rút quá số tiền đang có
        String query = "UPDATE wallets SET balance = balance - ? WHERE user_id = ? AND balance >= ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setBigDecimal(1, amount); // Số tiền rút
            pstmt.setString(2, userId);      // ID người dùng
            pstmt.setBigDecimal(3, amount); // Điều kiện: Số dư phải đủ để trừ

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu cập nhật thành công

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}