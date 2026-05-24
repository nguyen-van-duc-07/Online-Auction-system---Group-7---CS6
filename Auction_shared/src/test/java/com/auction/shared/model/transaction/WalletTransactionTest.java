package com.auction.shared.model.transaction;

import com.auction.shared.enums.WalletTransactionStatus;
import com.auction.shared.enums.WalletTransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class WalletTransactionTest {

    @Test
    @DisplayName("Tạo WalletTransaction thành công với dữ liệu hợp lệ")
    void testBuilder_ValidData_CreatesTransaction() {
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId("WALLET_123")
                .type(WalletTransactionType.DEPOSIT)
                .amount(new BigDecimal("1000.50"))
                .balanceBefore(new BigDecimal("5000.00"))
                .balanceAfter(new BigDecimal("6000.50"))
                .frozenBefore(BigDecimal.ZERO)
                .frozenAfter(BigDecimal.ZERO)
                .referenceId("REF_999")
                .status(WalletTransactionStatus.SUCCESS)
                .build();

        assertNotNull(transaction);
        assertEquals("WALLET_123", transaction.getWalletId());
        assertEquals(new BigDecimal("1000.50"), transaction.getAmount());
        assertEquals(new BigDecimal("5000.00"), transaction.getBalanceBefore());
    }

    @Test
    @DisplayName("Ném ngoại lệ IllegalArgumentException khi số tiền (amount) bằng 0")
    void testBuilder_ZeroAmount_ThrowsException() {
        BigDecimal invalidAmount = BigDecimal.ZERO;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            WalletTransaction.builder()
                    .walletId("WALLET_123")
                    .type(WalletTransactionType.BID_FREEZE)
                    .amount(invalidAmount)
                    .build();
        });

        assertEquals("Số tiền giao dịch phải lớn hơn 0!", exception.getMessage());
    }

    @Test
    @DisplayName("Ném ngoại lệ IllegalArgumentException khi số tiền (amount) bị âm")
    void testBuilder_NegativeAmount_ThrowsException() {
        BigDecimal invalidAmount = new BigDecimal("-50.00");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            WalletTransaction.builder()
                    .walletId("WALLET_123")
                    .type(WalletTransactionType.REFUND)
                    .amount(invalidAmount)
                    .build();
        });

        assertEquals("Số tiền giao dịch phải lớn hơn 0!", exception.getMessage());
    }

    @Test
    @DisplayName("Ném ngoại lệ NullPointerException khi walletId bị null")
    void testBuilder_NullWalletId_ThrowsException() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            WalletTransaction.builder()
                    .walletId(null)
                    .type(WalletTransactionType.DEPOSIT)
                    .amount(new BigDecimal("100.00"))
                    .build();
        });

        assertEquals("Mã ví (Wallet ID) không được để trống!", exception.getMessage());
    }
}