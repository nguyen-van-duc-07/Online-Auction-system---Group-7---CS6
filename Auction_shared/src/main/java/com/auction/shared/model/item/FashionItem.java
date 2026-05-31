package com.auction.shared.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.auction.shared.enums.ItemType;

@Getter
@Setter
@NoArgsConstructor
public class FashionItem extends Item {
    private String brand;
    private String size;
    private String material;
    private String gender;

    public FashionItem(String name, String description, java.util.Map<String, String> attributes) {
        super(name, ItemType.FASHION, description, attributes);
        if (attributes != null) {
            this.brand = attributes.get("brand");
            this.size = attributes.get("size");
            this.material = attributes.get("material");
            this.gender = attributes.get("gender");
        }
    }

    @Override
    public String getDisplayCategory() {
        return "Thời trang & Phụ kiện";
    }

    @Override
    public String printInfo() {
        return "Thương hiệu: " + (brand != null ? brand : "N/A") +
               " | Chất liệu: " + (material != null ? material : "N/A") +
               " | Size: " + (size != null ? size : "N/A");
    }
}
