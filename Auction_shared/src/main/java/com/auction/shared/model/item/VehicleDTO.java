package com.auction.shared.model.item;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder // Bắt buộc phải có để kế thừa khả năng build từ ItemDTO
public class VehicleDTO extends ItemDTO {
    private String brand;
    private int yearOfManufacture;
    private int mileage;
    private String engineType;
}