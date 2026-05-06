package com.auction.shared.model.item;
import com.auction.shared.enums.ItemType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter    // Tự tạo tất cả Getter
@SuperBuilder // Tự tạo toàn bộ logic Builder Pattern ở Cách 1 khi compile
public class ItemDTO {
    private String id;
    private LocalDateTime CreatedAt;
    private String name;
    private ItemType type;
    private String description;
}