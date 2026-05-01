package com.auction.shared.model;

import com.auction.shared.model.item.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    @DisplayName("Kiểm tra logic khởi tạo Art từ ArtDTO")
    void testArtModelLogic() {
        // 1. Giả lập DTO từ Database (có ID và ngày tạo)
        ArtDTO dto = ArtDTO.builder()
                .id("ART-001")
                .CreatedAt(LocalDateTime.now())
                .name("Mona Lisa")
                .basePrice(new BigDecimal("1000000"))
                .artistName("Leonardo da Vinci")
                .material("Sơn dầu")
                .yearCreated(1503)
                .hasCertificate(true)
                .build();

        // 2. Chuyển DTO vào Model
        Art art = new Art(dto);

        // 3. Kiểm tra (Assert)
        assertEquals("ART-001", art.getId());
        assertEquals("Mona Lisa", art.getName());
        assertTrue(art.getSpecificDetails().contains("Leonardo da Vinci"));
        assertTrue(art.getSpecificDetails().contains("Chứng nhận: Có"));
    }

    @Test
    @DisplayName("Kiểm tra logic khởi tạo Electronics từ ElectronicsDTO")
    void testElectronicsModelLogic() {
        // 1. Giả lập DTO tạo mới từ Giao diện (ID và ngày tạo là null)
        ElectronicsDTO dto = ElectronicsDTO.builder()
                .name("iPhone 15 Pro")
                .basePrice(new BigDecimal("25000000"))
                .brand("Apple")
                .warrantyMonths(12)
                .condition("Mới")
                .build();

        // 2. Chuyển DTO vào Model
        Electronics electronics = new Electronics(dto);

        // 3. Kiểm tra
        assertNull(electronics.getId(), "Sản phẩm mới tạo ID phải là null");
        assertEquals("Apple", electronics.getBrand());
        assertEquals(12, electronics.getWarrantyMonths());
        assertTrue(electronics.getSpecificDetails().contains("Bảo hành: 12 tháng"));
    }

    @Test
    @DisplayName("Kiểm tra tính đa hình của Item")
    void testPolymorphism() {
        // Tạo một danh sách các Item khác nhau
        ArtDTO artDto = ArtDTO.builder().name("Bức tranh").build();
        ElectronicsDTO elecDto = ElectronicsDTO.builder().name("Điện thoại").build();

        Item item1 = new Art(artDto);
        Item item2 = new Electronics(elecDto);

        // Kiểm tra xem chúng có cùng là Item không
        assertNotNull(item1);
        assertNotNull(item2);

        // Kiểm tra logic Overriding của phương thức abstract
        assertNotEquals(item1.getSpecificDetails(), item2.getSpecificDetails());
    }
}