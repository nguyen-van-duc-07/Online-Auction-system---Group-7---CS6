package com.auction.shared.model.factory;
import com.auction.shared.model.item.Art;
import com.auction.shared.model.item.Item;

import java.util.Map;
import java.math.BigDecimal;

public class ArtFactory implements ItemFactory {
    @Override
    public Item createItem(String name, String category, String description, String sellerId, String imageUrl, BigDecimal basePrice, Map<String, Object> specificData){
        String artistName = (String) specificData.get("artistName");
        String material = (String) specificData.get("material");
        int yearCreated = (int) specificData.get("yearCreated");
        boolean hasCertificate = (boolean) specificData.get("hasCertificate");
        return new Art(name, category, description, sellerId, imageUrl, basePrice, artistName, material, yearCreated, hasCertificate);
    }
}
