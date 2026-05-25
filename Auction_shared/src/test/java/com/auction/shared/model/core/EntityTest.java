package com.auction.shared.model.core;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {
    private static class DummyEntity extends Entity {
        public DummyEntity() {
            super();
        }
        public DummyEntity(String id, LocalDateTime createdAt) {
            super(id, createdAt);
        }
    }

    @Test
    void testProtectedDefaultConstructor() {
        DummyEntity entity = new DummyEntity();
        assertNotNull(entity.getId(), "ID không được để trống");
        assertDoesNotThrow(() -> UUID.fromString(entity.getId()), "ID sinh ra phải là một UUID hợp lệ");
        assertNotNull(entity.getCreatedAt(), "Thời gian tạo không được để trống");
        assertTrue(entity.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)),
                "Thời gian tạo phải sát với thời gian hiện hành");
    }

    @Test
    void testParameterizedConstructor_WithValidData() {
        String expectedId = "custom-id-123";
        LocalDateTime expectedTime = LocalDateTime.of(2026, 1, 1, 10, 30);
        DummyEntity entity = new DummyEntity(expectedId, expectedTime);
        assertEquals(expectedId, entity.getId(), "ID phải khớp với tham số truyền vào");
        assertEquals(expectedTime, entity.getCreatedAt(), "Thời gian phải khớp với tham số truyền vào");
    }

    @Test
    void testParameterizedConstructor_WithNullCreatedAt() {
        String expectedId = "custom-id-456";
        DummyEntity entity = new DummyEntity(expectedId, null);
        assertEquals(expectedId, entity.getId(), "ID phải khớp với tham số truyền vào");
        assertNotNull(entity.getCreatedAt(), "Thời gian phải tự động chuyển thành now() nếu tham số truyền vào là null");
    }
}