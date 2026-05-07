package com.auction.shared.model.item;

import com.auction.shared.enums.ItemType;
import com.auction.shared.model.core.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter // Tự động tạo Getter cho tất cả các trường
@Setter // Tự động tạo Setter cho tất cả các trường
@NoArgsConstructor // Tạo constructor không tham số (cần thiết cho một số Framework)
public class Item extends Entity {

    private String name;
    private ItemType type;
    private String description;

    public Item(String id, LocalDateTime createdAt, String name, ItemType type, String description) {
        super(id, createdAt);
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public Item(String name, ItemType type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public Item(ItemDTO dto) {
        // Nếu là hàng mới: getId() và getCreatedAt() sẽ tự động là null.
        // Nếu là hàng từ DB: getId() và getCreatedAt() sẽ mang giá trị thật.
        super(dto.getId(), dto.getCreatedAt());
        this.name = dto.getName();
        this.type = dto.getType();
        this.description = dto.getDescription();

    }
}