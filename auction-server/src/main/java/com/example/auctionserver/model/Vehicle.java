package com.example.auctionserver.model;

public class Vehicle extends Item{
    private String brand;
    private int mileage;
    private String engineType;
    public Vehicle(){}
    public Vehicle(String name, String description,int yearCreated, String brand,  int mileage, String engineType) {
        super(name, description, yearCreated);
        this.brand = brand;
        this.mileage = mileage;
        this.engineType = engineType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }
}
