package com.auction.shared.model.transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    // Tạo một class con giả lập (Test Stub) để có thể khởi tạo và test Abstract Class
    static class TestTransaction extends Transaction {
        public TestTransaction(String fromId, String toId) {
            super(fromId, toId);
        }
    }

    @Test
    @DisplayName("Should initialize successfully when IDs are valid")
    void shouldInitialize_WhenIdsAreValid() {
        Transaction transaction = new TestTransaction("USER_A", "USER_B");
        assertNotNull(transaction);
        assertEquals("USER_A", transaction.getFromId());
        assertEquals("USER_B", transaction.getToId());
    }

    @Test
    @DisplayName("Should throw NullPointerException when fromId is null")
    void shouldThrowException_WhenFromIdIsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new TestTransaction(null, "USER_B");
        });
        assertEquals("fromId must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw NullPointerException when toId is null")
    void shouldThrowException_WhenToIdIsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new TestTransaction("USER_A", null);
        });
        assertEquals("toId must not be null", exception.getMessage());
    }
}