package com.auction.shared.model.item;

import com.auction.shared.enums.ItemType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    static class DummyItem extends Item {
        public DummyItem(String id, java.time.LocalDateTime createdAt, String name, ItemType type, String description) {
            super(id, createdAt, name, type, description);
        }
        public DummyItem(String name, ItemType type, String description, Map<String, String> attributes) {
            super(name, type, description, attributes);
        }
        public DummyItem(ItemDTO dto) {
            super(dto);
        }
        @Override
        public String getDisplayCategory() { return "Dummy"; }
        @Override
        public String printInfo() { return "Dummy"; }
    }

    @Test
    @DisplayName("Kiểm thử Constructor đầy đủ thông tin cơ bản - Phải gán đúng các trường và ID/createdAt tùy chỉnh")
    void testConstructorWithAllFields() {
        String expectedId = "item-123";
        LocalDateTime expectedTime = LocalDateTime.of(2026, 5, 24, 10, 0);
        String expectedName = "Laptop Gaming";
        ItemType expectedType = ItemType.ELECTRONICS;
        String expectedDescription = "Laptop hiệu năng cao";

        Item item = new DummyItem(expectedId, expectedTime, expectedName, expectedType, expectedDescription);

        assertEquals(expectedId, item.getId(), "ID phải trùng khớp với ID truyền vào");
        assertEquals(expectedTime, item.getCreatedAt(), "Thời gian tạo phải trùng khớp với giá trị truyền vào");
        assertEquals(expectedName, item.getName(), "Tên sản phẩm phải trùng khớp");
        assertEquals(expectedType, item.getType(), "Loại sản phẩm phải trùng khớp");
        assertEquals(expectedDescription, item.getDescription(), "Mô tả sản phẩm phải trùng khớp");
    }

    @Test
    @DisplayName("Kiểm thử Constructor có thuộc tính bổ sung - Phải khởi tạo tự động ID, thời gian tạo từ Entity và gán đúng Map")
    void testConstructorWithAdditionalAttributes() {
        String expectedName = "Bàn phím cơ";
        ItemType expectedType = ItemType.ELECTRONICS;
        String expectedDescription = "Bàn phím cơ không dây";
        Map<String, String> expectedAttributes = new HashMap<>();
        expectedAttributes.put("switch", "Red Switch");
        expectedAttributes.put("layout", "TKL");

        Item item = new DummyItem(expectedName, expectedType, expectedDescription, expectedAttributes);

        assertNotNull(item.getId(), "ID phải được tự động sinh bởi lớp cha Entity");
        assertNotNull(item.getCreatedAt(), "Thời gian tạo phải được tự động sinh bởi lớp cha Entity");
        assertEquals(expectedName, item.getName(), "Tên sản phẩm phải trùng khớp");
        assertEquals(expectedType, item.getType(), "Loại sản phẩm phải trùng khớp");
        assertEquals(expectedDescription, item.getDescription(), "Mô tả sản phẩm phải trùng khớp");
        assertEquals(expectedAttributes, item.getAdditionalAttributes(), "Map thuộc tính bổ sung phải trùng khớp");
    }

    @Test
    @DisplayName("Kiểm thử Constructor từ ItemDTO - Phải sao chép chính xác tất cả thuộc tính của DTO")
    void testConstructorWithDto() {
        ItemDTO dto = new ItemDTO();
        dto.setId("dto-456");
        dto.setCreatedAt(LocalDateTime.now().minusDays(1));
        dto.setName("Chuột không dây");
        dto.setType(ItemType.ELECTRONICS);
        dto.setDescription("Chuột silent");
        
        Map<String, String> attributes = new HashMap<>();
        attributes.put("dpi", "16000");
        attributes.put("battery", "Rechargeable");
        dto.setAdditionalAttributes(attributes);

        Item item = new DummyItem(dto);

        assertEquals(dto.getId(), item.getId(), "ID phải được sao chép chính xác từ DTO");
        assertEquals(dto.getCreatedAt(), item.getCreatedAt(), "Thời gian tạo phải được sao chép chính xác từ DTO");
        assertEquals(dto.getName(), item.getName(), "Tên sản phẩm phải được sao chép chính xác từ DTO");
        assertEquals(dto.getType(), item.getType(), "Loại sản phẩm phải được sao chép chính xác từ DTO");
        assertEquals(dto.getDescription(), item.getDescription(), "Mô tả sản phẩm phải được sao chép chính xác từ DTO");
        assertEquals(dto.getAdditionalAttributes(), item.getAdditionalAttributes(), "Thuộc tính bổ sung phải được sao chép chính xác từ DTO");
    }
}