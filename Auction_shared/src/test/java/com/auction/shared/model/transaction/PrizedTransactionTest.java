package com.auction.shared.model.transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PrizedTransactionTest {
    @Test
    @DisplayName("Should create PrizedTransaction successfully with valid data")
    void shouldCreatePrizedTransaction_WhenDataIsValid() {
        PrizedTransaction transaction = new PrizedTransaction(
                "SENDER_123", "RECEIVER_456", "AUC_001", "ITEM_999", new BigDecimal("1500.00")
        );
        assertNotNull(transaction);
        assertEquals("SENDER_123", transaction.getFromId());
        assertEquals("AUC_001", transaction.getAuctionId());
        assertEquals(new BigDecimal("1500.00"), transaction.getFinalPrice());
    }

    @Test
    @DisplayName("Should throw Exception when auctionId is empty or blank")
    void shouldThrowException_WhenAuctionIdIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PrizedTransaction("SENDER_123", "RECEIVER_456", "   ", "ITEM_999", new BigDecimal("1500.00"));
        });
        assertEquals("Auction ID không được để trống.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw Exception when itemId is null")
    void shouldThrowException_WhenItemIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PrizedTransaction("SENDER_123", "RECEIVER_456", "AUC_001", null, new BigDecimal("1500.00"));
        });
        assertEquals("Item ID không được để trống.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw Exception when finalPrice is zero")
    void shouldThrowException_WhenFinalPriceIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PrizedTransaction("SENDER_123", "RECEIVER_456", "AUC_001", "ITEM_999", BigDecimal.ZERO);
        });
        assertEquals("Giá chốt (finalPrice) phải lớn hơn 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw Exception when finalPrice is negative")
    void shouldThrowException_WhenFinalPriceIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PrizedTransaction("SENDER_123", "RECEIVER_456", "AUC_001", "ITEM_999", new BigDecimal("-10.00"));
        });
        assertEquals("Giá chốt (finalPrice) phải lớn hơn 0.", exception.getMessage());
    }
}