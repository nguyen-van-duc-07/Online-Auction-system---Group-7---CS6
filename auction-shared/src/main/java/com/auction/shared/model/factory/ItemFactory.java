package com.auction.shared.model.factory;
import com.auction.shared.model.item.Item;
import com.auction.shared.model.item.ItemDTO;

import java.util.Map;
import java.math.BigDecimal;

public interface ItemFactory<T extends ItemDTO> {
    Item createItem(T dto);
}

