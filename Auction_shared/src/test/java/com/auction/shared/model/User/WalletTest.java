package com.auction.shared.model.User;

import com.auction.shared.model.user.Wallet;
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
        wallet = new Wallet();
    }

    // --- TEST PHẦN NẠP TIỀN (DEPOSIT) ---

    @Test
    @DisplayName("Nạp tiền hợp lệ - EP")
    void testDepositValid() {
        BigDecimal amount = new BigDecimal("100.00");
        wallet.deposit(amount);
        // Lưu ý: Với BigDecimal, assertEquals sẽ bị FAIL vì nó so cả độ chính xác scale (số chữ số thập phân)
        assertEquals(0, new BigDecimal("100.00").compareTo(wallet.getBalance()), "Số dư phải là 100");
    }

    @Test
    @DisplayName("Nạp số tiền bằng 0 - BVA biên dưới")
    void testDepositZero() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            wallet.deposit(BigDecimal.ZERO);
        });
        assertEquals("Số tiền nạp phải lớn hơn 0", exception.getMessage());
    }

    @Test
    @DisplayName("Nạp số tiền âm - EP không hợp lệ")
    void testDepositNegative() {
        assertThrows(IllegalArgumentException.class, () -> {
            wallet.deposit(new BigDecimal("-1.00"));
        });
    }

    // --- TEST PHẦN RÚT TIỀN (WITHDRAW) ---

    @Test
    @DisplayName("Rút tiền trong hạn mức khả dụng - EP hợp lệ")
    void testWithdrawValid() {
        wallet.deposit(new BigDecimal("100.00"));
        wallet.withdraw(new BigDecimal("40.00"));

        assertEquals(0, new BigDecimal("60.00").compareTo(wallet.getBalance()));
    }

    @Test
    @DisplayName("Rút đúng bằng số dư khả dụng - BVA biên trên")
    void testWithdrawAll() {
        wallet.deposit(new BigDecimal("100.00"));
        wallet.withdraw(new BigDecimal("100.00"));

        assertEquals(0, BigDecimal.ZERO.compareTo(wallet.getBalance()));
    }

    @Test
    @DisplayName("Rút quá số dư khả dụng - EP không hợp lệ")
    void testWithdrawOverBalance() {
        wallet.deposit(new BigDecimal("100.00"));
        wallet.freeze(new BigDecimal("50.00")); // Còn lại 50.00 khả dụng

        // Rút 50.01 (vượt biên 0.01)
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            wallet.withdraw(new BigDecimal("50.01"));
        });
        assertTrue(exception.getMessage().contains("không đủ"));
    }

    @Test
    @DisplayName("Rút tiền khi số dư tổng đủ nhưng số dư khả dụng thiếu")
    void testWithdrawWhenFrozen() {
        wallet.deposit(new BigDecimal("100.00"));
        wallet.freeze(new BigDecimal("80.00")); // Tổng 100, đóng băng 80, khả dụng 20

        // Rút 30 -> Phải lỗi
        assertThrows(IllegalStateException.class, () -> {
            wallet.withdraw(new BigDecimal("30.00"));
        });
    }
}