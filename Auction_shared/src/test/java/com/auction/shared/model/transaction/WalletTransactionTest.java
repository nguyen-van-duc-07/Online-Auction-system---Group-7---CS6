package com.auction.shared.model.transaction;

import com.auction.shared.enums.WalletTransactionStatus;
import com.auction.shared.enums.WalletTransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class WalletTransactionTest {
    @Test
    @DisplayName("Should create WalletTransaction successfully with valid data")
    void shouldCreateWalletTransaction_WhenDataIsValid(){
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
    @DisplayName("Should throw Exception when amount is zero")
    void shouldThrowException_WhenAmountIsZero() {
        BigDecimal invalidAmount = BigDecimal.ZERO;
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            WalletTransaction.builder()
                    .walletId("WALLET_123")
                    .type(WalletTransactionType.BID_FREEZE)
                    .amount(invalidAmount)
                    .build();
        });
        assertEquals("Transaction amount must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw Exception when amount is negative")
    void shouldThrowException_WhenAmountIsNegative() {
        BigDecimal invalidAmount = new BigDecimal("-50.00");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            WalletTransaction.builder()
                    .walletId("WALLET_123")
                    .type(WalletTransactionType.REFUND)
                    .amount(invalidAmount)
                    .build();
        });
        assertEquals("Transaction amount must be greater than zero", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw Exception when walletId is null")
    void shouldThrowException_WhenWalletIdIsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            WalletTransaction.builder()
                    .walletId(null)
                    .type(WalletTransactionType.DEPOSIT)
                    .amount(new BigDecimal("100.00"))
                    .build();
        });
        assertEquals("WalletId must not be null", exception.getMessage());
    }
}
