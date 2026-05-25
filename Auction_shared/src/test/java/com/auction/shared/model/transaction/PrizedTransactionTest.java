package com.auction.shared.model.transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PrizedTransactionTest {

    @Test
    @DisplayName("Khởi tạo thành công khi các tham số đều hợp lệ")
    void testConstructor_ValidData_AssignsAllFields() {
        PrizedTransaction transaction = new PrizedTransaction(
                "SENDER_123", "RECEIVER_456", "AUC_001", "ITEM_999", new BigDecimal("1500.00")
        );
        assertNotNull(transaction);
        assertEquals("SENDER_123", transaction.getFromId());
        assertEquals("AUC_001", transaction.getAuctionId());
        assertEquals(new BigDecimal("1500.00"), transaction.getFinalPrice());
    }

    @Test
    @DisplayName("Ném lỗi IllegalArgumentException khi auctionId bị null")
    void testConstructor_EmptyAuctionId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PrizedTransaction("SENDER_123", "RECEIVER_456", "   ", "ITEM_999", new BigDecimal("1500.00"));
        });
        assertEquals("Auction ID không được để trống.", exception.getMessage());
    }

    @Test
    @DisplayName("Ném ngoại lệ khi itemId bị null")
    void testConstructor_NullItemId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PrizedTransaction("SENDER_123", "RECEIVER_456", "AUC_001", null, new BigDecimal("1500.00"));
        });
        assertEquals("Item ID không được để trống.", exception.getMessage());
    }

    @Test
    @DisplayName("Ném ngoại lệ khi finalPrize bằng 0")
    void testConstructor_ZeroFinalPrice_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PrizedTransaction("SENDER_123", "RECEIVER_456", "AUC_001", "ITEM_999", BigDecimal.ZERO);
        });
        assertEquals("Giá chốt (finalPrice) phải lớn hơn 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Ném ngoại lệ khi finalPrize là số âm")
    void testConstructor_NegativeFinalPrice_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PrizedTransaction("SENDER_123", "RECEIVER_456", "AUC_001", "ITEM_999", new BigDecimal("-10.00"));
        });
        assertEquals("Giá chốt (finalPrice) phải lớn hơn 0.", exception.getMessage());
    }
}