package com.auction.shared.model.item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Electronics extends Item{
    private String brand;
    private int warrantyMonths;
    private String condition;
    //Constructor dùng cho tạo mới sản phẩm từ giao diện người bán.
    public Electronics(String name, String category, String description,
                       String sellerId, String imageUrl, BigDecimal basePrice,
                       String brand, int warrantyMonths, String condition){
        super(name, "Electronics", description, sellerId, imageUrl, basePrice);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
        this.condition = condition;
    }
    //Constructor dùng cho ItemDAO khi đọc dữ liệu từ MySQL.
    public Electronics(String id, LocalDateTime createdAt, String name, String category,
                       String description, String sellerId, String imageUrl,
                       BigDecimal basePrice, String brand, int warrantyMonths, String condition){
        super(id, createdAt, name, "Electronics", description, sellerId, imageUrl, basePrice);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
        this.condition = condition;
    }

    @Override
    public String getSpecificDetails() {
        return String.format("Thương hiệu: %s | Model: %s | Bảo hành: %d tháng | Tình trạng: %s",
                brand, name, warrantyMonths, condition);
    }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}
