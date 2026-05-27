package com.auction.shared.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.auction.shared.enums.ItemType;

@Getter
@Setter
@NoArgsConstructor
public class CollectibleItem extends Item {
    private String rarity;
    private String authentication;
    private String condition;

    public CollectibleItem(String name, String description, java.util.Map<String, String> attributes) {
        super(name, ItemType.COLLECTIBLES, description, attributes);
        if (attributes != null) {
            this.rarity = attributes.get("rarity");
            this.authentication = attributes.get("authentication");
            this.condition = attributes.get("condition");
        }
    }

    @Override
    public String getDisplayCategory() {
        return "Đồ sưu tầm cổ vật";
    }

    @Override
    public String printInfo() {
        return "Độ hiếm: " + (rarity != null ? rarity : "N/A") +
               " | Chứng thực: " + (authentication != null ? authentication : "N/A") +
               " | Tình trạng: " + (condition != null ? condition : "N/A");
    }
}
