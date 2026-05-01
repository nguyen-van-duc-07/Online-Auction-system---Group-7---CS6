package com.auction.shared.model.factory;

import com.auction.shared.model.item.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FactoryTest {

    @Test
    @DisplayName("Kiểm tra ArtFactory tạo đúng đối tượng Art")
    void testArtFactory() {
        // 1. Chuẩn bị DTO
        ArtDTO dto = ArtDTO.builder()
                .name("Thiếu nữ bên hoa huệ")
                .basePrice(new BigDecimal("500000000"))
                .artistName("Tô Ngọc Vân")
                .material("Sơn dầu")
                .build();

        // 2. Khởi tạo Factory (Vì hàm createItem KHÔNG phải static)
        ArtFactory factory = new ArtFactory();
        Art art = (Art) factory.createItem(dto);

        // 3. Kiểm chứng
        assertNotNull(art);
        assertEquals("Tô Ngọc Vân", art.getArtistName());
        assertTrue(art instanceof Art);
    }

    @Test
    @DisplayName("Kiểm tra ElectronicsFactory tạo đúng đối tượng Electronics")
    void testElectronicsFactory() {
        // 1. Chuẩn bị DTO
        ElectronicsDTO dto = ElectronicsDTO.builder()
                .name("MacBook Pro M3")
                .brand("Apple")
                .basePrice(new BigDecimal("45000000"))
                .warrantyMonths(12)
                .build();

        // 2. Khởi tạo Factory
        ElectronicsFactory factory = new ElectronicsFactory();
        Electronics electronics = (Electronics) factory.createItem(dto);

        // 3. Kiểm chứng
        assertNotNull(electronics);
        assertEquals("Apple", electronics.getBrand());
        assertTrue(electronics instanceof Electronics);
    }

    @Test
    @DisplayName("Kiểm tra VehicleFactory tạo đúng đối tượng Vehicle")
    void testVehicleFactory() {
        // 1. Chuẩn bị DTO
        VehicleDTO dto = VehicleDTO.builder()
                .name("VinFast VF8")
                .brand("VinFast")
                .basePrice(new BigDecimal("1200000000"))
                .engineType("Electric")
                .build();

        // 2. Khởi tạo Factory
        VehicleFactory factory = new VehicleFactory();
        Vehicle vehicle = (Vehicle) factory.createItem(dto);

        // 3. Kiểm chứng
        assertNotNull(vehicle);
        assertEquals("Electric", vehicle.getEngineType());
        assertTrue(vehicle instanceof Vehicle);
    }
}