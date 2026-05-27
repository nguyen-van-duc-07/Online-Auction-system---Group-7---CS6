package com.auction.shared.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.auction.shared.enums.ItemType;

@Getter
@Setter
@NoArgsConstructor
public class ElectronicItem extends Item {
    private String model;
    private String brand;
    private String warranty;
    private String condition;

    public ElectronicItem(String name, String description, java.util.Map<String, String> attributes) {
        super(name, ItemType.ELECTRONICS, description, attributes);
        if (attributes != null) {
            this.model = attributes.get("model");
            this.brand = attributes.get("brand");
            this.warranty = attributes.get("warranty");
            this.condition = attributes.get("condition");
        }
    }

    @Override
    public String getDisplayCategory() {
        return "Thiết bị điện tử";
    }

    @Override
    public String printInfo() {
        return "Hãng: " + (brand != null ? brand : "N/A") +
               " | Model: " + (model != null ? model : "N/A") +
               " | Tình trạng: " + (condition != null ? condition : "N/A");
    }
}
