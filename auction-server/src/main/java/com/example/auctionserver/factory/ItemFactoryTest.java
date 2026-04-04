package com.example.auctionserver.factory;

import com.example.auctionserver.dto.ItemDTO;
import com.example.auctionserver.model.Art;
import com.example.auctionserver.model.Electronics;
import com.example.auctionserver.model.Item;
import com.example.auctionserver.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItemFactoryTest {

    private ItemDTO dto;

    // Chạy trước mỗi hàm @Test để chuẩn bị một DTO mới, sạch sẽ
    @BeforeEach
    public void setUp() {
        dto = new ItemDTO();
    }

    @Test
    public void testCreateArt() {
        // 1. Chuẩn bị dữ liệu (Arrange)
        dto.setItemType("ART");
        dto.setName("Bức tranh Đêm Đầy Sao");
        dto.setDescription("Bản gốc");
        dto.setYearCreated(1889);
        dto.setArtistName("Vincent van Gogh");
        dto.setMaterial("Sơn dầu");
        dto.setHasCertificate(true);

        ItemFactory factory = new ArtFactory();

        // 2. Thực thi (Act)
        Item item = factory.createAndSetupItem(dto);

        // 3. Kiểm tra kết quả (Assert)
        assertNotNull(item, "Item không được null");
        assertTrue(item instanceof Art, "Item phải là instance của Art");

        // Ép kiểu để kiểm tra chi tiết
        Art art = (Art) item;

        // Kiểm tra thuộc tính chung kế thừa từ Item
        assertEquals("Bức tranh Đêm Đầy Sao", art.getName());
        assertEquals(1889, art.getYearCreated());

        // Kiểm tra thuộc tính riêng của Art
        assertEquals("Vincent van Gogh", art.getArtistName());
        assertEquals("Sơn dầu", art.getMaterial());
        assertTrue(art.isHasCertificate());
    }

    @Test
    public void testCreateVehicle() {
        // 1. Chuẩn bị dữ liệu
        dto.setItemType("VEHICLE");
        dto.setName("Siêu xe thể thao");
        dto.setYearCreated(2023);
        dto.setBrand("Ferrari");
        dto.setMileage(5000);
        dto.setEngineType("V8");

        ItemFactory factory = new VehicleFactory();

        // 2. Thực thi
        Item item = factory.createAndSetupItem(dto);

        // 3. Kiểm tra kết quả
        assertTrue(item instanceof Vehicle);

        Vehicle vehicle = (Vehicle) item;
        assertEquals("Siêu xe thể thao", vehicle.getName());
        assertEquals("Ferrari", vehicle.getBrand());
        assertEquals(5000, vehicle.getMileage());
        assertEquals("V8", vehicle.getEngineType());
    }

    @Test
    public void testCreateElectronics() {
        // 1. Chuẩn bị dữ liệu
        dto.setItemType("ELECTRONICS");
        dto.setName("Laptop Gaming");
        dto.setYearCreated(2024);
        dto.setBrand("Asus ROG");
        dto.setWarrantyMonths(24);
        dto.setWeight(2.5);

        ItemFactory factory = new ElectronicsFactory();

        // 2. Thực thi
        Item item = factory.createAndSetupItem(dto);

        // 3. Kiểm tra kết quả
        assertTrue(item instanceof Electronics);

        Electronics electronics = (Electronics) item;
        assertEquals("Laptop Gaming", electronics.getName());
        assertEquals("Asus ROG", electronics.getBrand());
        assertEquals(24, electronics.getWarrantyMonths());
        assertEquals(2.5, electronics.getWeight(), 0.001); // So sánh số thập phân cần có độ sai số (delta)
    }
}