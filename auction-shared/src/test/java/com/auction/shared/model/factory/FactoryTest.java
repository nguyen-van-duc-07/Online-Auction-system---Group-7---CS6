package com.auction.shared.model.factory;
import com.auction.shared.model.item.Item;
import com.auction.shared.model.factory.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class FactoryTest {
    static void main(String[] args) {
        // 1. Chuẩn bị dữ liệu chung
        String name = "Xe VinFast VF8";
        String desc = "Xe dien thong minh, mau xanh";
        String sellerId = "SELLER_001";
        String imageUrl = "vf8.jpg";
        BigDecimal price = new BigDecimal("1000000000");

        // 2. Chuẩn bị dữ liệu ĐẶC THÙ cho Vehicle
        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("brand", "VinFast");
        vehicleData.put("yearOfManufacture", 2023);
        vehicleData.put("mileage", 5000);
        vehicleData.put("engineType", "Electric");

        // 3. Sử dụng Factory để tạo đối tượng
        ItemFactory factory = new VehicleFactory();
        Item myCar = factory.createItem(name, "Vehicle", desc, sellerId, imageUrl, price, vehicleData);

        // 4. Kiểm tra kết quả
        System.out.println("--- KET QUA TEST FACTORY ---");
        System.out.println("Ten san pham: " + myCar.getName());
        System.out.println("Chi tiet dac thu: " + myCar.getSpecificDetails());
    }
}