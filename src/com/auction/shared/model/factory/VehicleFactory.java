package com.auction.shared.model.factory;
import com.auction.shared.model.item.Vehicle;
import com.auction.shared.model.item.Item;

import java.util.Map;
import java.math.BigDecimal;

public class VehicleFactory implements ItemFactory{
    @Override
    public Item createItem(String name, String category, String description, String sellerId, String imageUrl, BigDecimal basePrice, Map<String, Object> specificData){
        String brand = (String) specificData.get("brand");
        int yearOfManufacture = (int) specificData.get("yearOfManufacture");
        int mileage = (int) specificData.get("mileage");
        String engineType = (String) specificData.get("engineType");
        return new Vehicle(name, category, description, sellerId, imageUrl, basePrice, brand, yearOfManufacture, mileage, engineType);
    }
}
