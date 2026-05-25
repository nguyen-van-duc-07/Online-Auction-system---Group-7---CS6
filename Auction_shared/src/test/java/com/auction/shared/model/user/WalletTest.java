package com.auction.shared.model.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

// Import truyền thống của JUnit 5
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet("2323");
    }

    @Test
    @DisplayName("Nạp tiền hợp lệ")
    void testDepositValid() {
        BigDecimal amount = new BigDecimal("100.00");
        wallet.deposit(amount);
        assertEquals(0, new BigDecimal("100.00").compareTo(wallet.getBalance()), "Số dư phải là 100");
    }

    @Test
    @DisplayName("Nạp số tiền bằng 0")
    void testDepositZero() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            wallet.deposit(BigDecimal.ZERO);
        });
        assertEquals("Số tiền nạp phải lớn hơn 0", exception.getMessage());
    }

    @Test
    @DisplayName("Nạp số tiền âm")
    void testDepositNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.deposit(new BigDecimal("-1.00"));
        });
    }

    @Test
    @DisplayName("Rút tiền trong hạn mức khả dụng")
    void testWithdrawValid() {
        wallet.deposit(new BigDecimal("100.00"));
        wallet.withdraw(new BigDecimal("40.00"));

        assertEquals(0, new BigDecimal("60.00").compareTo(wallet.getBalance()));
    }

    @Test
    @DisplayName("Rút đúng bằng số dư khả dụng")
    void testWithdrawAll() {
        wallet.deposit(new BigDecimal("100.00"));
        wallet.withdraw(new BigDecimal("100.00"));

        assertEquals(0, BigDecimal.ZERO.compareTo(wallet.getBalance()));
    }

    @Test
    @DisplayName("Rút quá số dư khả dụng")
    void testWithdrawOverBalance() {
        wallet.deposit(new BigDecimal("100.00"));
        wallet.freeze(new BigDecimal("50.00"));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            wallet.withdraw(new BigDecimal("50.01"));
        });
        assertTrue(exception.getMessage().contains("không đủ"));
    }

    @Test
    @DisplayName("Rút tiền khi số dư tổng đủ nhưng số dư khả dụng thiếu")
    void testWithdrawWhenFrozen() {
        wallet.deposit(new BigDecimal("100.00"));
        wallet.freeze(new BigDecimal("80.00"));

        assertThrows(IllegalStateException.class, () -> {
            wallet.withdraw(new BigDecimal("30.00"));
        });
    }
}