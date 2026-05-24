package com.auction.shared.model.transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BidTransactionTest {
    @Test
    @DisplayName("Khởi tạo thành công khi các tham số đều hợp lệ")
    void testConstructor_ValidInputs_ShouldCreateSuccessfully() {
        String validAuctionId = "AUC-999";
        String validBidderId = "USER-123";
        BigDecimal validAmount = new BigDecimal("500000.00");

        BidTransaction transaction = new BidTransaction(validAuctionId, validBidderId, validAmount);

        assertNotNull(transaction);
        assertEquals(validAuctionId, transaction.getAuctionId());
        assertEquals(validBidderId, transaction.getBidderId());
        assertEquals(validAmount, transaction.getBidAmount());
    }

    @Test
    @DisplayName("Ném lỗi IllegalArgumentException khi auctionId bị null")
    void testConstructor_NullAuctionId_ShouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new BidTransaction(null, "USER-123", new BigDecimal("100000"));
        });
        assertEquals("Auction ID không được để trống.", exception.getMessage());
    }

    @Test
    @DisplayName("Ném lỗi IllegalArgumentException khi auctionId là chuỗi rỗng")
    void testConstructor_EmptyAuctionId_ShouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new BidTransaction("   ", "USER-123", new BigDecimal("100000"));
        });
        assertEquals("Auction ID không được để trống.", exception.getMessage());
    }

    @Test
    @DisplayName("Ném lỗi IllegalArgumentException khi bidderId bị null hoặc rỗng")
    void testConstructor_InvalidBidderId_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new BidTransaction("AUC-999", null, new BigDecimal("100000"));
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new BidTransaction("AUC-999", "", new BigDecimal("100000"));
        });
    }

    @Test
    @DisplayName("Ném lỗi IllegalArgumentException khi số tiền bid bị null")
    void testConstructor_NullBidAmount_ShouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new BidTransaction("AUC-999", "USER-123", null);
        });
        assertEquals("Số tiền bid phải lớn hơn 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Ném lỗi IllegalArgumentException khi số tiền bid bằng 0")
    void testConstructor_ZeroBidAmount_ShouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new BidTransaction("AUC-999", "USER-123", BigDecimal.ZERO);
        });
        assertEquals("Số tiền bid phải lớn hơn 0.", exception.getMessage());
    }

    @Test
    @DisplayName("Ném lỗi IllegalArgumentException khi số tiền bid là số âm")
    void testConstructor_NegativeBidAmount_ShouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new BidTransaction("AUC-999", "USER-123", new BigDecimal("-50000"));
        });
        assertEquals("Số tiền bid phải lớn hơn 0.", exception.getMessage());
    }
}