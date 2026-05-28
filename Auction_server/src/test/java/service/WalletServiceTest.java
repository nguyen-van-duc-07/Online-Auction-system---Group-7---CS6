package service;

import com.auction.shared.enums.WalletTransactionStatus;
import com.auction.shared.enums.WalletTransactionType;
import com.auction.shared.model.user.Wallet;
import com.auction.shared.model.transaction.WalletTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.SellerProfileRepository;
import repository.WalletRepository;
import repository.WalletTransactionRepository;
import config.ConnectionProvider;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepo;

    @Mock
    private WalletTransactionRepository txRepo;

    @Mock
    private SellerProfileRepository sellerProfileRepo;

    @Mock
    private ConnectionProvider connectionProvider;

    @Mock
    private NotificationService notifService;

    @Mock
    private Connection mockConnection;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(
            walletRepo,
            txRepo,
            sellerProfileRepo,
            connectionProvider,
            notifService
        );
    }

    // ==========================================
    // TEST CASES FOR getBalance
    // ==========================================

    @Test
    void getBalance_userExists_returnsCorrectBalance() throws Exception {
        // Arrange
        String userId = "user123";
        BigDecimal expectedBalance = new BigDecimal("1500000.00");
        Wallet wallet = new Wallet();
        wallet.setBidderId(userId);
        wallet.setBalance(expectedBalance);

        when(walletRepo.getWalletByUserId(userId)).thenReturn(wallet);

        // Act
        BigDecimal actualBalance = walletService.getBalance(userId);

        // Assert
        assertEquals(expectedBalance, actualBalance);
        verify(walletRepo).getWalletByUserId(userId);
    }

    @Test
    void getBalance_userDoesNotExist_throwsException() {
        // Arrange
        String userId = "unknownUser";
        when(walletRepo.getWalletByUserId(userId)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            walletService.getBalance(userId);
        });

        assertEquals("Không tìm thấy ví của người dùng: " + userId, exception.getMessage());
        verify(walletRepo).getWalletByUserId(userId);
    }

    // ==========================================
    // TEST CASES FOR freezeMoney
    // ==========================================

    @Test
    void freezeMoney_sufficientBalance_updatesWalletAndSavesTransaction() {
        // Arrange
        String userId = "user123";
        String auctionId = "auc999";
        BigDecimal amount = new BigDecimal("500000.00");

        Wallet wallet = new Wallet();
        wallet.setId("walletId123");
        wallet.setBidderId(userId);
        wallet.setBalance(new BigDecimal("1000000.00"));
        wallet.setFrozenBalance(BigDecimal.ZERO);

        when(walletRepo.getWalletByUserIdForUpdate(mockConnection, userId)).thenReturn(wallet);

        // Act
        walletService.freezeMoney(mockConnection, userId, amount, auctionId);

        // Assert
        assertEquals(new BigDecimal("500000.00"), wallet.getBalance());
        assertEquals(new BigDecimal("500000.00"), wallet.getFrozenBalance());
        verify(walletRepo).updateWallet(mockConnection, wallet);
        verify(txRepo).saveWalletTransaction(eq(mockConnection), any(WalletTransaction.class));
    }

    @Test
    void freezeMoney_insufficientBalance_throwsRuntimeException() {
        // Arrange
        String userId = "user123";
        String auctionId = "auc999";
        BigDecimal amount = new BigDecimal("1500000.00"); // Greater than 1,000,000

        Wallet wallet = new Wallet();
        wallet.setId("walletId123");
        wallet.setBidderId(userId);
        wallet.setBalance(new BigDecimal("1000000.00"));
        wallet.setFrozenBalance(BigDecimal.ZERO);

        when(walletRepo.getWalletByUserIdForUpdate(mockConnection, userId)).thenReturn(wallet);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            walletService.freezeMoney(mockConnection, userId, amount, auctionId);
        });

        assertEquals("Số dư không đủ để đặt giá", exception.getMessage());
        verify(walletRepo, never()).updateWallet(any(), any());
        verify(txRepo, never()).saveWalletTransaction(any(), any());
    }

    // ==========================================
    // TEST CASES FOR releaseFrozen
    // ==========================================

    @Test
    void releaseFrozen_validRequest_updatesWalletAndSavesTransaction() {
        // Arrange
        String userId = "user123";
        String auctionId = "auc999";
        BigDecimal amount = new BigDecimal("300000.00");

        Wallet wallet = new Wallet();
        wallet.setId("walletId123");
        wallet.setBidderId(userId);
        wallet.setBalance(new BigDecimal("700000.00"));
        wallet.setFrozenBalance(new BigDecimal("300000.00"));

        when(walletRepo.getWalletByUserIdForUpdate(mockConnection, userId)).thenReturn(wallet);

        // Act
        walletService.releaseFrozen(mockConnection, userId, amount, auctionId);

        // Assert
        assertEquals(new BigDecimal("1000000.00"), wallet.getBalance());
        assertEquals(0, BigDecimal.ZERO.compareTo(wallet.getFrozenBalance()));
        verify(walletRepo).updateWallet(mockConnection, wallet);
        verify(txRepo).saveWalletTransaction(eq(mockConnection), any(WalletTransaction.class));
    }

    // ==========================================
    // TEST CASES FOR createTransactionRequest
    // ==========================================

    @Test
    void createTransactionRequest_depositSuccess_commitsAndSendsNotification() throws Exception {
        // Arrange
        String userId = "user123";
        BigDecimal amount = new BigDecimal("500000.00");
        WalletTransactionType type = WalletTransactionType.DEPOSIT;

        Wallet wallet = new Wallet();
        wallet.setId("walletId123");
        wallet.setBidderId(userId);
        wallet.setBalance(new BigDecimal("1000000.00"));

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(walletRepo.getWalletByUserIdForUpdate(mockConnection, userId)).thenReturn(wallet);
        when(txRepo.saveWalletTransaction(eq(mockConnection), any(WalletTransaction.class))).thenReturn(true);

        // Act
        boolean result = walletService.createTransactionRequest(userId, amount, type);

        // Assert
        assertTrue(result);
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
        verify(notifService).sendFromNotification(any());
    }

    @Test
    void createTransactionRequest_withdrawInsufficientBalance_failsWithoutCommit() throws Exception {
        // Arrange
        String userId = "user123";
        BigDecimal amount = new BigDecimal("1500000.00"); // Greater than balance
        WalletTransactionType type = WalletTransactionType.WITHDRAW;

        Wallet wallet = new Wallet();
        wallet.setId("walletId123");
        wallet.setBidderId(userId);
        wallet.setBalance(new BigDecimal("1000000.00"));

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(walletRepo.getWalletByUserIdForUpdate(mockConnection, userId)).thenReturn(wallet);

        // Act
        boolean result = walletService.createTransactionRequest(userId, amount, type);

        // Assert
        assertFalse(result);
        verify(mockConnection).rollback();
        verify(txRepo, never()).saveWalletTransaction(any(), any());
        verify(notifService, never()).sendFromNotification(any());
    }

    // ==========================================
    // TEST CASES FOR processTransactionRequest
    // ==========================================

    @Test
    void processTransactionRequest_approveDeposit_updatesWalletAndCommits() throws Exception {
        // Arrange
        String transactionId = "tx777";
        WalletTransaction tx = WalletTransaction.builder()
            .walletId("walletId123")
            .type(WalletTransactionType.DEPOSIT)
            .amount(new BigDecimal("200000.00"))
            .status(WalletTransactionStatus.PENDING)
            .build();
        tx.setId(transactionId);

        Wallet wallet = new Wallet();
        wallet.setId("walletId123");
        wallet.setBidderId("user123");
        wallet.setBalance(new BigDecimal("1000000.00"));

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(txRepo.getTransactionById(mockConnection, transactionId)).thenReturn(tx);
        when(walletRepo.getWalletByWalletId(mockConnection, "walletId123")).thenReturn(wallet);
        when(walletRepo.updateWallet(mockConnection, wallet)).thenReturn(true);

        // Act
        boolean result = walletService.processTransactionRequest(transactionId, WalletTransactionStatus.APPROVE);

        // Assert
        assertTrue(result);
        assertEquals(new BigDecimal("1200000.00"), wallet.getBalance());
        assertEquals(WalletTransactionStatus.APPROVE, tx.getStatus());
        verify(txRepo).updateWalletTransaction(mockConnection, tx);
        verify(mockConnection).commit();
        verify(notifService).sendFromNotification(any());
    }

    @Test
    void processTransactionRequest_rejectWithdraw_refundsBalanceAndCommits() throws Exception {
        // Arrange
        String transactionId = "tx777";
        WalletTransaction tx = WalletTransaction.builder()
            .walletId("walletId123")
            .type(WalletTransactionType.WITHDRAW)
            .amount(new BigDecimal("300000.00"))
            .status(WalletTransactionStatus.PENDING)
            .build();
        tx.setId(transactionId);

        Wallet wallet = new Wallet();
        wallet.setId("walletId123");
        wallet.setBidderId("user123");
        wallet.setBalance(new BigDecimal("700000.00")); // Amount has been deducted when withdraw was requested

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(txRepo.getTransactionById(mockConnection, transactionId)).thenReturn(tx);
        when(walletRepo.getWalletByWalletId(mockConnection, "walletId123")).thenReturn(wallet);
        when(txRepo.updateWalletTransaction(mockConnection, tx)).thenReturn(true);

        // Act
        boolean result = walletService.processTransactionRequest(transactionId, WalletTransactionStatus.REJECT);

        // Assert
        assertTrue(result);
        assertEquals(new BigDecimal("1000000.00"), wallet.getBalance()); // Refunded
        assertEquals(WalletTransactionStatus.REJECT, tx.getStatus());
        verify(walletRepo).updateWallet(mockConnection, wallet);
        verify(mockConnection).commit();
        verify(notifService).sendFromNotification(any());
    }
}
