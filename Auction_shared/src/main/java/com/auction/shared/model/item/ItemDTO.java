package com.auction.shared.model.item;
import com.auction.shared.enums.ItemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter    // Tự tạo tất cả Getter
@Setter
@NoArgsConstructor
public class ItemDTO implements Serializable {
    private String id;
    private LocalDateTime CreatedAt;
    private String name;
    private ItemType type;
    private String description;
    private java.util.Map<String, String> additionalAttributes;
}