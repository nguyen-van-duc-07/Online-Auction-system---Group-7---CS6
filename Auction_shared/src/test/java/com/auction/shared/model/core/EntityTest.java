package com.auction.shared.model.core;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {
    private static class TestEntity extends Entity {
        public TestEntity() {
            super();
        }

        public TestEntity(String id, LocalDateTime createdAt) {
            super(id, createdAt);
        }

        // Tạo một hàm public để giả lập việc JPA tự động gọi @PrePersist
        public void triggerPrePersist() {
            this.onPrePersist();
        }
    }

    @Test
    void testDefaultConstructor_ShouldLeaveFieldsNullInitially() {
        // Khởi tạo đối tượng rỗng (chưa qua bước Save của JPA)
        TestEntity entity = new TestEntity();

        // Lúc này cả ID và Thời gian đều phải là null vì JPA chưa can thiệp
        assertNull(entity.getId(), "ID phải là null trước khi được JPA tự động sinh ra");
        assertNull(entity.getCreatedAt(), "Thời gian tạo phải là null trước khi gọi @PrePersist");
    }

    @Test
    void testPrePersist_ShouldGenerateCreatedAt() {
        TestEntity entity = new TestEntity();

        // Giả lập hành động chuẩn bị lưu vào Database (JPA trigger callback)
        entity.triggerPrePersist();

        assertNull(entity.getId(), "ID vẫn null vì nó phụ thuộc vào @GeneratedValue khi thực sự chạy câu lệnh SQL");
        assertNotNull(entity.getCreatedAt(), "Thời gian tạo (createdAt) phải được sinh ra sau khi chạy @PrePersist");
    }

    @Test
    void testAllArgsConstructor_ShouldKeepProvidedValues() {
        String expectedId = "custom-id-123";
        LocalDateTime expectedTime = LocalDateTime.of(2026, 1, 1, 10, 0);

        TestEntity entity = new TestEntity(expectedId, expectedTime);

        // Kích hoạt PrePersist để đảm bảo logic if(createdAt == null) hoạt động đúng
        entity.triggerPrePersist();

        assertEquals(expectedId, entity.getId(), "ID không khớp với giá trị truyền vào");
        assertEquals(expectedTime, entity.getCreatedAt(), "Thời gian tạo bị ghi đè sai lệch so với giá trị truyền vào");
    }
}