package com.auction.shared.model.factory;
import com.auction.shared.model.item.*;

import java.util.Map;
import java.math.BigDecimal;

public class ArtFactory implements ItemFactory<ArtDTO> {
    @Override
    public Item createItem(ArtDTO dto) {
        return new Art(dto);
    }
}
