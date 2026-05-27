package com.auction.shared.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.auction.shared.enums.ItemType;

@Getter
@Setter
@NoArgsConstructor
public class SportItem extends Item {
    private String sportType;
    private String brand;
    private String weightSize;

    public SportItem(String name, String description, java.util.Map<String, String> attributes) {
        super(name, ItemType.SPORTS, description, attributes);
        if (attributes != null) {
            this.sportType = attributes.get("sportType");
            this.brand = attributes.get("brand");
            this.weightSize = attributes.get("weightSize");
        }
    }

    @Override
    public String getDisplayCategory() {
        return "Dụng cụ thể thao";
    }

    @Override
    public String printInfo() {
        return "Bộ môn: " + (sportType != null ? sportType : "N/A") +
               " | Thương hiệu: " + (brand != null ? brand : "N/A") +
               " | Kích cỡ/Trọng lượng: " + (weightSize != null ? weightSize : "N/A");
    }
}
