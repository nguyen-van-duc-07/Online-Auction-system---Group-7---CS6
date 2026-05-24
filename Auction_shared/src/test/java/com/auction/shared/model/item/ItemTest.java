package com.auction.shared.model.item;

import com.auction.shared.enums.ItemType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {
    @Test
    void testFullConstructor_AssignsAllFields() {
        String expectedId = "item-123";
        LocalDateTime expectedTime = LocalDateTime.of(2026, 5, 24, 10, 0);
        String expectedName = "Laptop Gaming";
        ItemType expectedType = ItemType.ELECTRONICS;
        String expectedDescription = "Laptop hiệu năng cao";

        Item item = new Item(expectedId, expectedTime, expectedName, expectedType, expectedDescription);

        assertEquals(expectedId, item.getId());
        assertEquals(expectedTime, item.getCreatedAt());
        assertEquals(expectedName, item.getName());
        assertEquals(expectedType, item.getType());
        assertEquals(expectedDescription, item.getDescription());
    }

    @Test
    void testPartialConstructor_GeneratesIdAndDate() {
        String expectedName = "Bàn phím cơ";
        ItemType expectedType = ItemType.ELECTRONICS;
        String expectedDescription = "Bàn phím cơ không dây";

        Item item = new Item(expectedName, expectedType, expectedDescription);

        assertNotNull(item.getId());
        assertNotNull(item.getCreatedAt());

        assertEquals(expectedName, item.getName());
        assertEquals(expectedType, item.getType());
        assertEquals(expectedDescription, item.getDescription());
    }

    @Test
    void testDTOConstructor_MapsDataCorrectly() {
        ItemDTO dto = new ItemDTO();
        dto.setId("dto-456");
        dto.setCreatedAt(LocalDateTime.now().minusDays(1));
        dto.setName("Chuột không dây");
        dto.setType(ItemType.ELECTRONICS);
        dto.setDescription("Chuột silent");

        Item item = new Item(dto);

        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getCreatedAt(), item.getCreatedAt());
        assertEquals(dto.getName(), item.getName());
        assertEquals(dto.getType(), item.getType());
        assertEquals(dto.getDescription(), item.getDescription());
    }
}