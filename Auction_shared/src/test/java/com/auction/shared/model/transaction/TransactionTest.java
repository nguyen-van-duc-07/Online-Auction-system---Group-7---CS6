package com.auction.shared.model.transaction;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {
    static class TestTransaction extends Transaction {
        public TestTransaction(String fromId, String toId) {
            super(fromId, toId);
        }
    }

    @Test
    @DisplayName("Khởi tạo thành công khi IDs hợp lệ")
    void shouldInitialize_WhenIdsAreValid() {
        Transaction transaction = new TestTransaction("USER_A", "USER_B");
        assertNotNull(transaction);
        assertEquals("USER_A", transaction.getFromId());
        assertEquals("USER_B", transaction.getToId());
    }

    @Test
    @DisplayName("Ném ngoại lệ NullPointerException khi fromId bị null")
    void shouldThrowException_WhenFromIdIsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new TestTransaction(null, "USER_B");
        });
        assertEquals("fromId không đuợc có giá trị null", exception.getMessage());
    }

    @Test
    @DisplayName("Ném ngoại lệ NullPointerException khi toId bị null")
    void shouldThrowException_WhenToIdIsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            new TestTransaction("USER_A", null);
        });
        assertEquals("toId không đuợc có giá trị null", exception.getMessage());
    }
}