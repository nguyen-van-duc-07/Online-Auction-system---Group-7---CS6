package service;

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

  public void freezeMoney(Connection conn, String userId, BigDecimal amount, String auctionId) {
    System.out.println("[WALLET - FREEZE] Yêu cầu đóng băng " + Format.fmt(amount) + " của User: " + userId + " (Auction: " + auctionId + ")");
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
    System.out.println("[WALLET - FREEZE] Thành công! User: " + userId
        + " | Số dư: " + Format.fmt(balBefore) + " -> " + Format.fmt(wallet.getBalance())
        + " | Đóng băng: " + Format.fmt(frozBefore) + " -> " + Format.fmt(wallet.getFrozenBalance()));
  }

  public void releaseFrozen(Connection conn, String userId, BigDecimal amount, String auctionId) {
    System.out.println("[WALLET - RELEASE] Yêu cầu hoàn trả " + Format.fmt(amount) + " cho User: " + userId + " (Auction: " + auctionId + ")");    Wallet wallet = walletRepo.getWalletByUserIdForUpdate(conn, userId);

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
        + " | Đóng băng: " + Format.fmt(frozBefore) + " -> " + Format.fmt(wallet.getFrozenBalance()));  }
}