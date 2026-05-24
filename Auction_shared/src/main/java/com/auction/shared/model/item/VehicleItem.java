package com.auction.shared.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VehicleItem extends Item {
    private String condition;
    private String mileage;
    private String manufactureYear;
    private String licensePlate;
    private String engine;
}
