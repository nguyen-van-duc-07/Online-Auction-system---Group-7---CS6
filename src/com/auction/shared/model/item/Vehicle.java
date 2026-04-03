package com.auction.shared.model.item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Vehicle extends Item{
    private String brand;
    private int yearOfManufacture, mileage;
    private String engineType;
    //Constructor dùng cho tạo mới sản phẩm từ giao diện người bán.
    public Vehicle(String name, String category, String description,
                   String sellerId, String imageUrl, BigDecimal basePrice,
                   String brand, int yearOfManufacture, int mileage, String engineType){
        super(name, "Vehicle", description, sellerId, imageUrl, basePrice);
        this.brand = brand;
        this.yearOfManufacture = yearOfManufacture;
        this.mileage = mileage;
        this.engineType = engineType;
    }
    //Constructor dùng cho ItemDAO khi đọc dữ liệu từ MySQL.
    public Vehicle(String id, LocalDateTime createdAt, String name, String category, String description,
                   String sellerId, String imageUrl, BigDecimal basePrice,
                   String brand, int yearOfManufacture, int mileage, String engineType){
        super(id, createdAt, name, "Vehicle", description, sellerId, imageUrl, basePrice);
        this.brand = brand;
        this.yearOfManufacture = yearOfManufacture;
        this.mileage = mileage;
        this.engineType = engineType;
    }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getYearOfManufacture() { return yearOfManufacture; }
    public void setYearOfManufacture(int yearOfManufacture) { this.yearOfManufacture = yearOfManufacture; }
    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }
    public String getEngineType() { return engineType; }
    public void setEngineType(String engineType) { this.engineType = engineType; }
    @Override
    public String getSpecificDetails() {
        return String.format("Hãng: %s | Năm SX: %d | ODO: %d km | Động cơ: %s",
                brand, yearOfManufacture, mileage, engineType);
    }
}
