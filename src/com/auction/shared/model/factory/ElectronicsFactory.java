package com.auction.shared.model.factory;
import com.auction.shared.model.item.Electronics;
import com.auction.shared.model.item.Item;

import java.util.Map;
import java.math.BigDecimal;

public class ElectronicsFactory implements ItemFactory {
    @Override
    public Item createItem(String name, String category, String description, String sellerId, String imageUrl, BigDecimal basePrice, Map<String, Object> specificData){
        String brand = (String) specificData.get("brand");
        int warranty = (int) specificData.get("warrantyMonths");
        String condition = (String) specificData.get("condition");
        return new Electronics(name, category, description, sellerId, imageUrl, basePrice, brand, warranty, condition);
    }
}
