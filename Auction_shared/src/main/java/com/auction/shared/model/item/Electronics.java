package com.auction.shared.model.item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Electronics extends Item{
    private String brand;
    private int warrantyMonths;
    private String condition;
    public Electronics(ElectronicsDTO dto) {
        // Đẩy toàn bộ thuộc tính cơ bản lên cho Item xử lý
        super(dto);

        // Tự xử lý các thuộc tính riêng của Electronics
        this.brand = dto.getBrand();
        this.warrantyMonths = dto.getWarrantyMonths();
        this.condition = dto.getCondition();
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