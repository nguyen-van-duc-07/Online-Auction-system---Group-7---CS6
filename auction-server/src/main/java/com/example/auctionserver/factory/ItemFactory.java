package com.example.auctionserver.factory;

import com.example.auctionserver.dto.ItemDTO;
import com.example.auctionserver.model.Item;

import java.util.Map;

public abstract class ItemFactory {
    public Item createAndSetupItem(ItemDTO dto){
        Item item = createItem();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setYearCreated(dto.getYearCreated());
        setupItem(item, dto);
        return item;
    }

    protected abstract Item createItem();
    protected abstract void setupItem(Item item, ItemDTO dto);

}
