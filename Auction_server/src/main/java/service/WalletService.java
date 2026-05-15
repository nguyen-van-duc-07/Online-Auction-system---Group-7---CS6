package service;

import com.auction.shared.model.order.Order;
import config.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;

import com.auction.shared.enums.WalletTransactionStatus;
import com.auction.shared.enums.WalletTransactionType;
import com.auction.shared.model.transaction.WalletTransaction;
import com.auction.shared.model.user.Wallet;
import repository.SellerProfileRepository;
import repository.WalletRepository;
import repository.WalletTransactionRepository;
import repository.debug.Format;

import java.sql.Connection;

public class WalletService {
  private final WalletRepository walletRepo = new WalletRepository();
  private final WalletTransactionRepository txRepo = new WalletTransactionRepository();
  private final SellerProfileRepository sellerProfileRepo = new SellerProfileRepository();

  /**
   * Lấy số dư hiện tại của người dùng.
   * Trả về BigDecimal để đảm bảo độ chính xác của tiền tệ.
   *
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
  public void processPayment(Connection conn, Order order) {
    System.out.println("BAT DAU QUA TRINH THANH TOAN CHO ORDER: " + order.getId());
    // 1. Xử lý ví buyer
    Wallet buyerWallet = walletRepo.getWalletByUserIdForUpdate(conn, order.getBuyerId());
    BigDecimal buyerBalBefore = buyerWallet.getBalance();
    BigDecimal buyerFrozBefore = buyerWallet.getFrozenBalance();
    buyerWallet.withdraw(order.getFinalPrice()); // tru 100% trong balance
    walletRepo.updateWallet(conn, buyerWallet); // cap nhat lai trong database
    // Tao lich su tru tien thanh toan
    WalletTransaction buyerTx = new WalletTransaction(
        buyerWallet.getId(),
        WalletTransactionType.AUCTION_PAYMENT,
        order.getFinalPrice().negate(),
        buyerBalBefore,
        buyerWallet.getBalance(),
        buyerFrozBefore,
        buyerWallet.getFrozenBalance(),
        order.getId(),
        WalletTransactionStatus.SUCCESS
    );
    // Luu lai lich su giao dich
    txRepo.saveWalletTransaction(conn, buyerTx);
    System.out.println(">>>DA TRU TIEN CUA WINNER " + Format.fmt(buyerTx.getAmount()));
    buyerBalBefore = buyerWallet.getBalance();
    buyerFrozBefore = buyerWallet.getFrozenBalance();
    buyerWallet.unfreeze(order.getDepositAmount());
    // Cap nhat lai du lieu vi sau khi hoan tien
    walletRepo.updateWallet(conn, buyerWallet);
    // Tao lich su giao dich khi hoan tien (tam dung chung object voi giao dich tru tien)
    buyerTx = new WalletTransaction(
        buyerWallet.getId(),
        WalletTransactionType.BID_RELEASE,
        order.getDepositAmount(),
        buyerBalBefore,
        buyerWallet.getBalance(),
        buyerFrozBefore,
        buyerWallet.getFrozenBalance(),
        order.getId(),
        WalletTransactionStatus.SUCCESS
    );
    txRepo.saveWalletTransaction(conn, buyerTx);
    System.out.println(">>>DA HOAN TIEN COC CHO WINNER " + Format.fmt(buyerTx.getAmount()));
    // 2. Xử lý ví seller
    String sellerId = sellerProfileRepo.getUserIdByProfileId(order.getSellerProfileId());
    Wallet sellerWallet = walletRepo.getWalletByUserIdForUpdate(conn, sellerId);
    BigDecimal sellerBalBefore = sellerWallet.getBalance();

    sellerWallet.deposit(order.getFinalPrice());  // nhận 100%
    walletRepo.updateWallet(conn, sellerWallet);

    WalletTransaction sellerTx = new WalletTransaction(
        sellerWallet.getId(),
        WalletTransactionType.SELLER_PAYOUT,
        order.getFinalPrice(),
        sellerBalBefore,
        sellerWallet.getBalance(),
        sellerWallet.getFrozenBalance(),
        sellerWallet.getFrozenBalance(),
        order.getId(),
        WalletTransactionStatus.SUCCESS
    );
    txRepo.saveWalletTransaction(conn, sellerTx);
    System.out.println(">>>DA CONG TIEN LAI CHO NGUOI BAN " + Format.fmt(sellerTx.getAmount()));
  }
  public void processCancelPenalty(Connection conn, Order order) {
    // 1. Xử lý ví buyer — mất cọc
    Wallet buyerWallet = walletRepo.getWalletByUserIdForUpdate(conn, order.getBuyerId());
    BigDecimal buyerBalBefore  = buyerWallet.getBalance();
    BigDecimal buyerFrozBefore = buyerWallet.getFrozenBalance();

    buyerWallet.unfreeze(order.getDepositAmount());   // giải phóng khỏi frozen
    buyerWallet.withdraw(order.getDepositAmount());   // trừ khỏi balance (mất cọc)
    walletRepo.updateWallet(conn, buyerWallet);

    WalletTransaction buyerTx = new WalletTransaction(
        buyerWallet.getId(),
        WalletTransactionType.REFUND,
        order.getDepositAmount().negate(),
        buyerBalBefore,
        buyerWallet.getBalance(),
        buyerFrozBefore,
        buyerWallet.getFrozenBalance(),
        order.getId(),
        WalletTransactionStatus.SUCCESS
    );
    txRepo.saveWalletTransaction(conn, buyerTx);

    // 2. Xử lý ví seller — nhận cọc phạt
    String sellerId = sellerProfileRepo.getUserIdByProfileId(order.getSellerProfileId());
    Wallet sellerWallet = walletRepo.getWalletByUserIdForUpdate(conn, sellerId);
    BigDecimal sellerBalBefore = sellerWallet.getBalance();

    sellerWallet.deposit(order.getDepositAmount());
    walletRepo.updateWallet(conn, sellerWallet);

    WalletTransaction sellerTx = new WalletTransaction(
        sellerWallet.getId(),
        WalletTransactionType.SELLER_PAYOUT,
        order.getDepositAmount(),
        sellerBalBefore,
        sellerWallet.getBalance(),
        sellerWallet.getFrozenBalance(),
        sellerWallet.getFrozenBalance(),
        order.getId(),
        WalletTransactionStatus.SUCCESS
    );
    txRepo.saveWalletTransaction(conn, sellerTx);

    System.out.println("[CANCEL] Buyer mất cọc: " + Format.fmt(order.getDepositAmount())
        + " | Seller nhận: " + Format.fmt(order.getDepositAmount()));
  }
}