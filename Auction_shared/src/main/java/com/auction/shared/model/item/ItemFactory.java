package com.auction.shared.model.item;

import com.auction.shared.enums.ItemType;
import java.util.Map;

public final class ItemFactory {

    private ItemFactory() {
    }

    public static Item createItem(
            String name,
            ItemType type,
            String description,
            Map<String, String> attributes) {
        
        ItemType actualType = type != null ? type : ItemType.OTHER;
        
        return switch (actualType) {
            case ELECTRONICS -> new ElectronicItem(name, description, attributes);
            case ARTS -> new ArtItem(name, description, attributes);
            case VEHICLES -> new VehicleItem(name, description, attributes);
            case FASHION -> new FashionItem(name, description, attributes);
            case SPORTS -> new SportItem(name, description, attributes);
            case COLLECTIBLES -> new CollectibleItem(name, description, attributes);
            default -> new GenericItem(name, actualType, description, attributes);
        };
    }
}
