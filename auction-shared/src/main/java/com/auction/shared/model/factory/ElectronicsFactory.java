package com.auction.shared.model.factory;
import com.auction.shared.model.item.*;

import java.util.Map;
import java.math.BigDecimal;

public class ElectronicsFactory implements ItemFactory<ElectronicsDTO> {
    @Override
    public Item createItem(ElectronicsDTO dto) {
        return new Electronics(dto);
    }
}
