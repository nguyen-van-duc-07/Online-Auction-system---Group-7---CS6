package com.auction.shared.model.item;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class Vehicle extends Item{
    private String brand;
    private int yearOfManufacture, mileage;
    private String engineType;
    public Vehicle(VehicleDTO dto) {
        super(dto); // Truyền dto lên Item, Item sẽ tự lấy các field chung
        this.brand = dto.getBrand();
        this.yearOfManufacture = dto.getYearOfManufacture();
        this.mileage = dto.getMileage();
        this.engineType = dto.getEngineType();
    }
    @Override
    public String getSpecificDetails() {
        return String.format("Hãng: %s | Năm SX: %d | ODO: %d km | Động cơ: %s",
                brand, yearOfManufacture, mileage, engineType);
    }
}
