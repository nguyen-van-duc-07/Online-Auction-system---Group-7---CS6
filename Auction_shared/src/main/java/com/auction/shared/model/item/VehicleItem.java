package com.auction.shared.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.auction.shared.enums.ItemType;

@Getter
@Setter
@NoArgsConstructor
public class VehicleItem extends Item {
    private String condition;
    private String mileage;
    private String manufactureYear;
    private String licensePlate;
    private String engine;

    public VehicleItem(String name, String description, java.util.Map<String, String> attributes) {
        super(name, ItemType.VEHICLES, description, attributes);
        if (attributes != null) {
            this.condition = attributes.get("condition");
            this.mileage = attributes.get("mileage");
            this.manufactureYear = attributes.get("manufactureYear");
            this.licensePlate = attributes.get("licensePlate");
            this.engine = attributes.get("engine");
        }
    }

    @Override
    public String getDisplayCategory() {
        return "Phương tiện giao thông";
    }

    @Override
    public String printInfo() {
        return "Động cơ: " + (engine != null ? engine : "N/A") +
               " | Odo: " + (mileage != null ? mileage : "N/A") + " km" +
               " | Năm sản xuất: " + (manufactureYear != null ? manufactureYear : "N/A");
    }
}
