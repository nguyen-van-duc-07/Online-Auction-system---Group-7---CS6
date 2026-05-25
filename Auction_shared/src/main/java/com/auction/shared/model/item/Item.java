package com.auction.shared.model.item;

import com.auction.shared.enums.ItemType;
import com.auction.shared.model.core.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Item extends Entity {

    private String name;
    private ItemType type;
    private String description;
    private java.util.Map<String, String> additionalAttributes;

    public Item(String id, LocalDateTime createdAt, String name, ItemType type, String description) {
        super(id, createdAt);
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public Item(String name, ItemType type, String description, java.util.Map<String, String> additionalAttributes) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.additionalAttributes = additionalAttributes;
    }

    public Item(ItemDTO dto) {
        super(dto.getId(), dto.getCreatedAt());
        this.name = dto.getName();
        this.type = dto.getType();
        this.description = dto.getDescription();
        this.additionalAttributes = dto.getAdditionalAttributes();
    }
}