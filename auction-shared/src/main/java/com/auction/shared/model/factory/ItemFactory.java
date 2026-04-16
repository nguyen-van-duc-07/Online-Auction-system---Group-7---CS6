package com.auction.shared.model.factory;
import com.auction.shared.model.item.Item;

import java.util.Map;
import java.math.BigDecimal;

public interface ItemFactory {
    Item createItem(String name, String category, String description, String sellerId, String imageUrl, BigDecimal basePrice, Map<String, Object> specificData);
}

