package service;

import com.auction.shared.model.order.Order;
import com.auction.shared.util.FormatUtil;
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

import java.sql.Connection;
import java.util.List;

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
    // 1. Cập nhật ví
    wallet.setBalance(wallet.getBalance().subtract(amount));
    wallet.setFrozenBalance(wallet.getFrozenBalance().add(amount));
    walletRepo.updateWallet(conn, wallet);
    // 2. Ghi nhận giao dịch
    WalletTransaction tx = new WalletTransaction(
        wallet.getId(),
        WalletTransactionType.BID_FREEZE,
        amount,
        auctionId,
        WalletTransactionStatus.SUCCESS
    );
    txRepo.saveWalletTransaction(conn, tx);
    System.out.println("[WALLET - FREEZE] Thành công! User: " + userId);
  }

  public void releaseFrozen(Connection conn, String userId, BigDecimal amount, String auctionId) {
    System.out.println("[WALLET - RELEASE] Yêu cầu hoàn trả " + amount + " cho User: " + userId + " (Auction: " + auctionId + ")");
    Wallet wallet = walletRepo.getWalletByUserIdForUpdate(conn, userId);

    wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(amount));
    wallet.setBalance(wallet.getBalance().add(amount));
    walletRepo.updateWallet(conn, wallet);

    WalletTransaction tx = new WalletTransaction(
        wallet.getId(),
        WalletTransactionType.BID_RELEASE,
        amount,
        auctionId,
        WalletTransactionStatus.SUCCESS
    );
    txRepo.saveWalletTransaction(conn, tx);
    System.out.println("[WALLET - RELEASE] Thành công! User: " + userId);
  }

  /**
   * Hàm cập nhật số dư ví trực tiếp dưới Database.
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

  public boolean createTransactionRequest(String userId, BigDecimal amount, WalletTransactionType type) {
    WalletTransaction tx = WalletTransaction.builder()
        .walletId(walletRepo.getWalletByUserId(userId).getId()) // Giả sử walletId lấy từ userId
        .type(type)
        .amount(amount)
        .status(WalletTransactionStatus.PENDING)
        .build();

    try (Connection conn = DatabaseConnection.getConnection()) {
      return txRepo.saveWalletTransaction(conn, tx);
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public List<WalletTransaction> getPendingTransactions() {
    try (Connection conn = DatabaseConnection.getConnection()) {
      return txRepo.findPendingTransactions(conn);
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public boolean processTransactionRequest(String transactionId, WalletTransactionStatus actionStatus) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        WalletTransaction tx = txRepo.getTransactionById(conn, transactionId);
        if (tx == null || tx.getStatus() != WalletTransactionStatus.PENDING) {
          return false;
        }

        if (actionStatus == WalletTransactionStatus.APPROVE) {
          // Tính toán balance mới
          Wallet wallet = walletRepo.getWalletByWalletId(conn, tx.getWalletId());
          if (wallet == null) return false;

          BigDecimal balBefore = wallet.getBalance();
          BigDecimal balAfter = balBefore;

          if (tx.getType() == WalletTransactionType.DEPOSIT) {
            balAfter = balBefore.add(tx.getAmount());
            wallet.setBalance(balAfter);
            walletRepo.updateWallet(conn, wallet);
          } else if (tx.getType() == WalletTransactionType.WITHDRAW) {
            if (balBefore.compareTo(tx.getAmount()) < 0) {
              return false; // Không đủ tiền
            }
            balAfter = balBefore.subtract(tx.getAmount());
            wallet.setBalance(balAfter);
            walletRepo.updateWallet(conn, wallet);
          }

          tx.setStatus(WalletTransactionStatus.APPROVE);

          txRepo.updateWalletTransaction(conn, tx);
        } else if (actionStatus == WalletTransactionStatus.REJECT) {
          tx.setStatus(WalletTransactionStatus.REJECT);
          txRepo.updateWalletTransaction(conn, tx);
        }

        conn.commit();
        return true;
      } catch (Exception e) {
        conn.rollback();
        e.printStackTrace();
        return false;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public void processPayment(Connection conn, Order order) {
    System.out.println("BAT DAU QUA TRINH THANH TOAN CHO ORDER: " + order.getId());
    // 1. Xử lý ví buyer
    BigDecimal remaining = order.getFinalPrice().subtract(order.getDepositAmount()); // 90%
    Wallet buyerWallet = walletRepo.getWalletByUserIdForUpdate(conn, order.getBuyerId());
    buyerWallet.unfreeze(order.getDepositAmount());
    buyerWallet.withdraw(remaining);
    walletRepo.updateWallet(conn, buyerWallet); // cap nhat lai trong database
    // Tao lich su tru tien thanh toan
    WalletTransaction buyerTx = new WalletTransaction(
        buyerWallet.getId(),
        WalletTransactionType.AUCTION_PAYMENT,
        remaining,
        order.getId(),
        WalletTransactionStatus.SUCCESS
    );
    // Luu lai lich su giao dich
    txRepo.saveWalletTransaction(conn, buyerTx);

    // 2. Xử lý ví seller
    String sellerId = sellerProfileRepo.getUserIdByProfileId(order.getSellerProfileId());
    Wallet sellerWallet = walletRepo.getWalletByUserIdForUpdate(conn, sellerId);

    sellerWallet.deposit(order.getFinalPrice());  // nhận 100%
    walletRepo.updateWallet(conn, sellerWallet);

    WalletTransaction sellerTx = new WalletTransaction(
        sellerWallet.getId(),
        WalletTransactionType.SELLER_PAYOUT,
        order.getFinalPrice(),
        order.getId(),
        WalletTransactionStatus.SUCCESS
    );
    txRepo.saveWalletTransaction(conn, sellerTx);
    System.out.println(">>>DA CONG TIEN LAI CHO NGUOI BAN " + com.auction.shared.util.FormatUtil.fmt(sellerTx.getAmount()));
  }

  public void processCancelPenalty(Connection conn, Order order) {
    // 1. Xử lý ví buyer — mất cọc
    Wallet buyerWallet = walletRepo.getWalletByUserIdForUpdate(conn, order.getBuyerId());

    buyerWallet.unfreeze(order.getDepositAmount());   // giải phóng khỏi frozen
    buyerWallet.withdraw(order.getDepositAmount());   // trừ khỏi balance (mất cọc)
    walletRepo.updateWallet(conn, buyerWallet);

    WalletTransaction buyerTx = new WalletTransaction(
        buyerWallet.getId(),
        WalletTransactionType.REFUND,
        order.getDepositAmount(),
        order.getId(),
        WalletTransactionStatus.SUCCESS
    );
    txRepo.saveWalletTransaction(conn, buyerTx);

    // 2. Xử lý ví seller — nhận cọc phạt
    String sellerId = sellerProfileRepo.getUserIdByProfileId(order.getSellerProfileId());
    Wallet sellerWallet = walletRepo.getWalletByUserIdForUpdate(conn, sellerId);

    sellerWallet.deposit(order.getDepositAmount());
    walletRepo.updateWallet(conn, sellerWallet);

    WalletTransaction sellerTx = new WalletTransaction(
        sellerWallet.getId(),
        WalletTransactionType.SELLER_PAYOUT,
        order.getDepositAmount(),
        order.getId(),
        WalletTransactionStatus.SUCCESS
    );
    txRepo.saveWalletTransaction(conn, sellerTx);

    System.out.println("[CANCEL] Buyer mất cọc: " + com.auction.shared.util.FormatUtil.fmt(order.getDepositAmount())
        + " | Seller nhận: " + com.auction.shared.util.FormatUtil.fmt(order.getDepositAmount()));
  }
}