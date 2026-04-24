package com.auction.shared.model.item;

import com.auction.shared.model.core.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Getter // Tự động tạo Getter cho tất cả các trường
@Setter // Tự động tạo Setter cho tất cả các trường
@NoArgsConstructor // Tạo constructor không tham số (cần thiết cho một số Framework)
public abstract class Item extends Entity {

    protected String name;
    protected String category;
    protected String description;
    protected String sellerId;
    protected String imageUrl;
    protected BigDecimal basePrice;

    // Chỉ cần MỘT constructor dùng DTO này để gánh cả 2 trường hợp
    public Item(ItemDTO dto) {
        // Nếu là hàng mới: getId() và getCreatedAt() sẽ tự động là null.
        // Nếu là hàng từ DB: getId() và getCreatedAt() sẽ mang giá trị thật.
        super(dto.getId(), dto.getCreatedAt());

        this.name = dto.getName();
        this.category = dto.getCategory();
        this.description = dto.getDescription();
        this.sellerId = dto.getSellerId();
        this.imageUrl = dto.getImageUrl();
        this.basePrice = dto.getBasePrice();
    }


    public abstract String getSpecificDetails();
}