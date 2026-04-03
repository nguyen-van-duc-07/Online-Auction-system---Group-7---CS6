package com.auction.test;

import com.auction.shared.model.item.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ModelTest {
    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU KIỂM TRA MODEL ===\n");

        // 1. Tạo danh sách chứa các Item (Tính đa hình)
        List<Item> testItems = new ArrayList<>();

        // 2. Thêm một món đồ điện tử
        testItems.add(new Electronics(
                "Laptop Dell XPS 13",
                "Electronics",
                "Máy dùng văn phòng cực mượt",
                "SELLER_001",
                "dell.jpg",
                new BigDecimal("15000000"),
                "Dell", 12, "99%"
        ));

        // 3. Thêm một tác phẩm nghệ thuật
        testItems.add(new Art(
                "Mona Lisa",
                "Art",
                "Bản sao giới hạn",
                "SELLER_002",
                "monalisa.png",
                new BigDecimal("50000000"),
                "Leonardo da Vinci", "Sơn dầu", 1503, true
        ));

        // 4. Thêm một chiếc xe
        testItems.add(new Vehicle(
                "Toyota Camry",
                "Vehicle",
                "Xe gia đình ít đi",
                "SELLER_003",
                "camry.jpg",
                new BigDecimal("800000000"),
                "Toyota", 2021, 15000, "Xăng"
        ));

        // 5. Duyệt danh sách và in kết quả
        for (Item item : testItems) {
            System.out.println("-------------------------------------------");
            System.out.println("ID: " + item.getId());
            System.out.println("Tên: " + item.getName());
            System.out.println("Loại: " + item.getCategory());
            System.out.println("Giá khởi điểm: " + item.getBasePrice() + " VNĐ");

            // ĐÂY LÀ CHỖ QUAN TRỌNG: Gọi hàm đa hình
            System.out.println("Chi tiết đặc thù: " + item.getSpecificDetails());
        }

        System.out.println("\n=== KIỂM TRA HOÀN TẤT ===");
    }
}