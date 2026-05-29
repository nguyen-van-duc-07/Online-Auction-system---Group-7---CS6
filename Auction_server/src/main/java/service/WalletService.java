package service;

import com.auction.shared.model.order.Order;
import com.auction.shared.util.CurrencyUtils;
import com.auction.shared.util.NotificationTemplate;
import config.DatabaseConnection;

import java.math.BigDecimal;

import com.auction.shared.enums.WalletTransactionStatus;
import com.auction.shared.enums.WalletTransactionType;
import com.auction.shared.model.transaction.WalletTransaction;
import com.auction.shared.model.user.Wallet;
import repository.SellerProfileRepository;
import repository.WalletRepository;
import repository.WalletTransactionRepository;

import java.sql.Connection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WalletService {
  private static final Logger log = LoggerFactory.getLogger(WalletService.class);
  private final WalletRepository walletRepo;
  private final WalletTransactionRepository txRepo;
  private final SellerProfileRepository sellerProfileRepo;
  private final config.ConnectionProvider connectionProvider;
  private final NotificationService notifService;

  /**
   * Constructor mặc định cho Production (không làm thay đổi code cũ).
   * Tự động khởi tạo và liên kết các Repository và Service liên quan.
   */
  public WalletService() {
    this(
        new WalletRepository(),
        new WalletTransactionRepository(),
        new SellerProfileRepository(),
        DatabaseConnection::getConnection,
        new NotificationService()
    );
  }

  /**
   * Constructor nhận tham số phục vụ cho Unit Test (phạm vi package-private).
   * Cho phép Mockito tiêm (inject) các đối tượng giả lập.
   */
  WalletService(
      WalletRepository walletRepo,
      WalletTransactionRepository txRepo,
      SellerProfileRepository sellerProfileRepo,
      config.ConnectionProvider connectionProvider,
      NotificationService notifService
  ) {
    this.walletRepo = walletRepo;
    this.txRepo = txRepo;
    this.sellerProfileRepo = sellerProfileRepo;
    this.connectionProvider = connectionProvider;
    this.notifService = notifService;
  }

  /**
   * Lấy số dư hiện tại của người dùng.
   *
   * @param userId Mã định danh của người dùng
   * @return Số dư hiện tại dưới dạng BigDecimal
   * @throws Exception nếu không tìm thấy ví
   */
  public BigDecimal getBalance(String userId) throws Exception {
    Wallet wallet = walletRepo.getWalletByUserId(userId);
    if (wallet != null) {
      return wallet.getBalance();
    } else {
      throw new Exception("Không tìm thấy ví của người dùng: " + userId);
    }
  }

  public void freezeMoney(Connection conn, String userId, BigDecimal amount, String auctionId) {
    log.info("[WALLET - FREEZE] Yêu cầu đóng băng {} của User: {} (Auction: {})", amount, userId, auctionId);
    Wallet wallet = walletRepo.getWalletByUserIdForUpdate(conn, userId);
    if (wallet.getBalance().compareTo(amount) < 0) {
      throw new RuntimeException("Số dư không đủ để đặt giá");
    }

    // 1. Cập nhật ví
    wallet.freeze(amount);
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
  }

  public void releaseFrozen(Connection conn, String userId, BigDecimal amount, String auctionId) {
    Wallet wallet = walletRepo.getWalletByUserIdForUpdate(conn, userId);
    wallet.unfreeze(amount);
    walletRepo.updateWallet(conn, wallet);

    WalletTransaction tx = new WalletTransaction(
        wallet.getId(),
        WalletTransactionType.BID_RELEASE,
        amount,
        auctionId,
        WalletTransactionStatus.SUCCESS
    );
    txRepo.saveWalletTransaction(conn, tx);
  }

  public boolean createTransactionRequest(String userId, BigDecimal amount, WalletTransactionType type) {
    try (Connection conn = connectionProvider.getConnection()) {
      conn.setAutoCommit(false);
      try {
        Wallet wallet = walletRepo.getWalletByUserIdForUpdate(conn, userId);
        if (wallet == null) {
          throw new Exception("Không tìm thấy ví cho người dùng: " + userId);
        }

        // Nếu là giao dịch RÚT TIỀN: Kiểm tra số dư khả dụng và tạm trừ luôn
        if (type == WalletTransactionType.WITHDRAW) {
          if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Số dư khả dụng không đủ để tạo yêu cầu rút tiền!");
          }
          wallet.withdraw(amount);
          walletRepo.updateWallet(conn, wallet);
        }

        WalletTransaction tx = WalletTransaction.builder()
            .walletId(wallet.getId())
            .type(type)
            .amount(amount)
            .status(WalletTransactionStatus.PENDING)
            .build();

        boolean success = txRepo.saveWalletTransaction(conn, tx);
        if (success) {
          conn.commit();
          switch (type) {
            case WITHDRAW -> notifService.sendFromNotification(
                NotificationTemplate.withdrawSubmitted(userId, amount)
            );
            case DEPOSIT -> notifService.sendFromNotification(
                NotificationTemplate.depositSubmitted(userId, amount)
            );
            default -> {
            }
          }
        } else {
          conn.rollback();
        }
        return success;
      } catch (Exception e) {
        conn.rollback();
        log.error("Lỗi khi tạo yêu cầu giao dịch cho user: {}", userId, e);
        return false;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (Exception e) {
      log.error("Lỗi kết nối cơ sở dữ liệu khi tạo yêu cầu giao dịch cho user: {}", userId, e);
      return false;
    }
  }

  public List<WalletTransaction> getPendingTransactions() {
    try (Connection conn = connectionProvider.getConnection()) {
      return txRepo.findPendingTransactions(conn);
    } catch (Exception e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy danh sách giao dịch đang xử lý", e);
      return null;
    }
  }

  public boolean processTransactionRequest(String transactionId, WalletTransactionStatus actionStatus) {
    try (Connection conn = connectionProvider.getConnection()) {
      conn.setAutoCommit(false);
      com.auction.shared.model.notification.Notification notificationToSend = null;
      try {
        WalletTransaction tx = txRepo.getTransactionById(conn, transactionId);
        if (tx == null || tx.getStatus() != WalletTransactionStatus.PENDING) {
          return false;
        }
        Wallet wallet = walletRepo.getWalletByWalletId(conn, tx.getWalletId());
        if (wallet == null) return false;
        if (actionStatus == WalletTransactionStatus.APPROVE) {
          if (tx.getType() == WalletTransactionType.DEPOSIT) {
            wallet.deposit(tx.getAmount());
            boolean success = walletRepo.updateWallet(conn, wallet);
            if (success) {
              notificationToSend = NotificationTemplate.depositApproved(wallet.getBidderId(), tx.getAmount(), wallet.getBalance());
            }
          } else if (tx.getType() == WalletTransactionType.WITHDRAW) {
            // Tiền rút đã được trừ từ lúc tạo yêu cầu, không cần trừ lại nữa
            notificationToSend = NotificationTemplate.withdrawApproved(wallet.getBidderId(), tx.getAmount(), wallet.getBalance());
          }

          tx.setStatus(WalletTransactionStatus.APPROVE);
          txRepo.updateWalletTransaction(conn, tx);
        } else if (actionStatus == WalletTransactionStatus.REJECT) {
          tx.setStatus(WalletTransactionStatus.REJECT);
          if (txRepo.updateWalletTransaction(conn, tx)) {
            switch (tx.getType()) {
              case DEPOSIT -> notificationToSend = NotificationTemplate.depositRejected(wallet.getBidderId(), tx.getAmount(), wallet.getBalance());
              case WITHDRAW -> {
                // Rút tiền bị từ chối -> Cộng hoàn trả lại số dư khả dụng
                wallet.deposit(tx.getAmount());
                walletRepo.updateWallet(conn, wallet);
                notificationToSend = NotificationTemplate.withdrawRejected(wallet.getBidderId(), tx.getAmount(), wallet.getBalance());
              }
            }
          }
        }

        conn.commit();

        // Gửi thông báo sau khi transaction DB đã commit thành công
        if (notificationToSend != null) {
          notifService.sendFromNotification(notificationToSend);
        }

        return true;
      } catch (Exception e) {
        conn.rollback();
        log.error("Lỗi khi xử lý duyệt giao dịch ID: {}", transactionId, e);
        return false;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (Exception e) {
      log.error("Lỗi cơ sở dữ liệu khi xử lý duyệt giao dịch ID: {}", transactionId, e);
      return false;
    }
  }

  public void processPayment(Connection conn, Order order) {
    log.info("Bắt đầu quá trình thanh toán cho đơn hàng: {}", order.getId());
    // 1. Xử lý ví buyer
    Wallet buyerWallet = walletRepo.getWalletByUserIdForUpdate(conn, order.getBuyerId());
    buyerWallet.payWinningAuction(order.getDepositAmount(), order.getRemainingAmount());
    walletRepo.updateWallet(conn, buyerWallet); // cap nhat lai trong database
    WalletTransaction buyerTx = new WalletTransaction(
        buyerWallet.getId(),
        WalletTransactionType.AUCTION_PAYMENT,
        order.getRemainingAmount(),
        order.getId(),
        WalletTransactionStatus.SUCCESS
    );
    // Luu lai lich su giao dich
    txRepo.saveWalletTransaction(conn, buyerTx);
    // 2. Xử lý ví seller
    Wallet sellerWallet = walletRepo.getWalletByUserIdForUpdate(conn, order.getSellerId());
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
    log.info(">>> Đã cộng tiền vào ví người bán: {}", CurrencyUtils.formatVnd(sellerTx.getAmount()));
  }

  public void processCancelPenalty(Connection conn, Order order) {
    // 1. Xử lý ví buyer — mất cọc
    Wallet buyerWallet = walletRepo.getWalletByUserIdForUpdate(conn, order.getBuyerId());
    buyerWallet.penaltyDeposit(order.getDepositAmount());
    walletRepo.updateWallet(conn, buyerWallet);

    // 2. Xử lý ví seller — nhận cọc phạt
    Wallet sellerWallet = walletRepo.getWalletByUserIdForUpdate(conn, order.getSellerId());

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

    log.info("[CANCEL] Người mua mất cọc: {} | Người bán nhận: {}", 
        CurrencyUtils.formatVnd(order.getDepositAmount()),
        CurrencyUtils.formatVnd(order.getDepositAmount()));
  }
}