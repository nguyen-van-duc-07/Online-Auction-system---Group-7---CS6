package com.example.auctionserver.model;

public class Electronics extends Item{
    private String brand;
    private int warrantyMonths;
    private double weight;
    public Electronics(){}
    public Electronics(String name, String description, int yearCreated, String brand, int warrantyMonths, double weight) {
        super(name, description, yearCreated);
        this.brand = brand;
        this.warrantyMonths = warrantyMonths;
        this.weight = weight;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
