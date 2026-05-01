package com.auction.shared.model.item;

import com.auction.shared.model.item.Vehicle;
import com.auction.shared.model.item.VehicleDTO;
import com.auction.shared.model.factory.VehicleFactory;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {

    @Test
    void testVehicleCreationFromDTO() {
        // 1. Chuẩn bị dữ liệu (Arrange)
        VehicleDTO dto = VehicleDTO.builder()
                .name("VinFast VF9")
                .brand("VinFast")
                .basePrice(new BigDecimal("1500000000"))
                .mileage(0)
                .engineType("Electric")
                .build();

        // 2. Thực thi (Act)
        Vehicle vehicle = new Vehicle(dto);

        // 3. Kiểm tra kết quả (Assert)
        assertEquals("VinFast VF9", vehicle.getName());
        assertEquals("VinFast", vehicle.getBrand());
        assertEquals("Electric", vehicle.getEngineType());
        assertTrue(vehicle.getSpecificDetails().contains("VinFast"));
    }

    @Test
    void testVehicleFactory() {
        // 1. Chuẩn bị
        VehicleFactory factory = new VehicleFactory();
        VehicleDTO dto = VehicleDTO.builder()
                .name("Porsche 911")
                .brand("Porsche")
                .yearOfManufacture(2024)
                .build();

        // 2. Thực thi qua Factory
        var item = factory.createItem(dto);

        // 3. Kiểm tra
        assertNotNull(item);
        assertTrue(item instanceof Vehicle);
        Vehicle vehicle = (Vehicle) item;
        assertEquals("Porsche", vehicle.getBrand());
    }
}