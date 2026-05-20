package com.auction.shared.model.core;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    // Tạo một lớp con giả (Dummy) để test vì Entity là abstract class
    private static class DummyEntity extends Entity {
        public DummyEntity() {
            super(); // Gọi protected constructor
        }

        public DummyEntity(String id, LocalDateTime createdAt) {
            super(id, createdAt); // Gọi public constructor
        }
    }

    @Test
    void testProtectedDefaultConstructor() {
        // Khởi tạo không tham số
        DummyEntity entity = new DummyEntity();

        // 1. Kiểm tra ID không bị null và phải đúng định dạng UUID
        assertNotNull(entity.getId(), "ID không được để trống");
        assertDoesNotThrow(() -> UUID.fromString(entity.getId()), "ID sinh ra phải là một UUID hợp lệ");

        // 2. Kiểm tra createdAt không bị null và phải là thời gian hiện tại
        assertNotNull(entity.getCreatedAt(), "Thời gian tạo không được để trống");

        // Đảm bảo thời gian tạo không vượt quá thời gian hệ thống hiện tại
        assertTrue(entity.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)),
                "Thời gian tạo phải sát với thời gian hiện hành");
    }

    @Test
    void testParameterizedConstructor_WithValidData() {
        // Chuẩn bị dữ liệu
        String expectedId = "custom-id-123";
        LocalDateTime expectedTime = LocalDateTime.of(2026, 1, 1, 10, 30);

        // Khởi tạo có tham số
        DummyEntity entity = new DummyEntity(expectedId, expectedTime);

        // Kiểm tra xem dữ liệu có được gán đúng không
        assertEquals(expectedId, entity.getId(), "ID phải khớp với tham số truyền vào");
        assertEquals(expectedTime, entity.getCreatedAt(), "Thời gian phải khớp với tham số truyền vào");
    }

    @Test
    void testParameterizedConstructor_WithNullCreatedAt() {
        // Chuẩn bị dữ liệu với thời gian null
        String expectedId = "custom-id-456";

        // Khởi tạo có tham số nhưng truyền null cho createdAt
        DummyEntity entity = new DummyEntity(expectedId, null);

        // Kiểm tra logic fallback: Nếu truyền null, hệ thống phải tự gán LocalDateTime.now()
        assertEquals(expectedId, entity.getId(), "ID phải khớp với tham số truyền vào");
        assertNotNull(entity.getCreatedAt(), "Thời gian phải tự động chuyển thành now() nếu tham số truyền vào là null");
    }

    @Test
    void testLombokGettersAndSetters() {
        DummyEntity entity = new DummyEntity();

        String newId = "updated-id-789";
        LocalDateTime newTime = LocalDateTime.now().minusDays(5);

        // Chạy Setters (được sinh ra bởi @Setter)
        entity.setId(newId);
        entity.setCreatedAt(newTime);

        // Chạy Getters (được sinh ra bởi @Getter) và so sánh
        assertEquals(newId, entity.getId(), "Lombok @Setter/@Getter cho ID hoạt động không đúng");
        assertEquals(newTime, entity.getCreatedAt(), "Lombok @Setter/@Getter cho createdAt hoạt động không đúng");
    }
}