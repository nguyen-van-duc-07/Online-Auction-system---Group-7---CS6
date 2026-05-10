package com.auction.shared.model.item;
import com.auction.shared.enums.ItemType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter    // Tự tạo tất cả Getter
@SuperBuilder // Tự tạo toàn bộ logic Builder Pattern ở Cách 1 khi compile
public class ItemDTO implements Serializable {
    private String id;
    private LocalDateTime CreatedAt;
    private String name;
    private ItemType type;
    private String description;
    public  ItemDTO(Item item) {
        this.id = item.getId();
        this.CreatedAt = item.getCreatedAt();
        this.name = item.getName();
        this.type = item.getType();
        this.description = item.getDescription();
    }
}