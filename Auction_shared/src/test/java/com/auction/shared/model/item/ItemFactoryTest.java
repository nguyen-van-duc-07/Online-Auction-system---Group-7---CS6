package com.auction.shared.model.item;

import com.auction.shared.enums.ItemType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ItemFactoryTest {

    @Test
    @DisplayName("Kiểm thử ItemFactory.createItem - Tạo sản phẩm ELECTRONICS phải trả về đúng ElectronicItem")
    void testCreateElectronics() {
        Item item = ItemFactory.createItem(
            "Laptop Asus",
            ItemType.ELECTRONICS,
            "Laptop gaming xịn",
            Map.of("brand", "Asus", "model", "ROG Strix", "condition", "New")
        );

        assertTrue(item instanceof ElectronicItem, "Phải là instance của ElectronicItem");
        assertEquals("Thiết bị điện tử", item.getDisplayCategory());
        assertTrue(item.printInfo().contains("Asus"));
        assertTrue(item.printInfo().contains("ROG Strix"));
    }

    @Test
    @DisplayName("Kiểm thử ItemFactory.createItem - Tạo sản phẩm ARTS phải trả về đúng ArtItem")
    void testCreateArts() {
        Item item = ItemFactory.createItem(
            "Mona Lisa",
            ItemType.ARTS,
            "Bức tranh nổi tiếng",
            Map.of("artist", "Da Vinci", "year", "1503", "dimension", "77x53cm")
        );

        assertTrue(item instanceof ArtItem, "Phải là instance của ArtItem");
        assertEquals("Tác phẩm nghệ thuật", item.getDisplayCategory());
        assertTrue(item.printInfo().contains("Da Vinci"));
        assertTrue(item.printInfo().contains("1503"));
    }

    @Test
    @DisplayName("Kiểm thử ItemFactory.createItem - Tạo sản phẩm VEHICLES phải trả về đúng VehicleItem")
    void testCreateVehicles() {
        Item item = ItemFactory.createItem(
            "Tesla Model 3",
            ItemType.VEHICLES,
            "Xe điện Tesla",
            Map.of("engine", "Electric 283hp", "mileage", "12000", "manufactureYear", "2023")
        );

        assertTrue(item instanceof VehicleItem, "Phải là instance của VehicleItem");
        assertEquals("Phương tiện giao thông", item.getDisplayCategory());
        assertTrue(item.printInfo().contains("Electric 283hp"));
        assertTrue(item.printInfo().contains("12000 km"));
    }

    @Test
    @DisplayName("Kiểm thử ItemFactory.createItem - Tạo sản phẩm OTHER phải trả về đúng GenericItem")
    void testCreateOther() {
        Item item = ItemFactory.createItem(
            "Móc khóa lưu niệm",
            ItemType.OTHER,
            "Móc khóa xinh xắn",
            Map.of("color", "Red")
        );

        assertTrue(item instanceof GenericItem, "Phải là instance của GenericItem");
        assertEquals("Sản phẩm khác", item.getDisplayCategory());
        assertTrue(item.printInfo().contains("Móc khóa xinh xắn"));
    }
}
