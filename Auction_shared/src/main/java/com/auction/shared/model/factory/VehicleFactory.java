package com.auction.shared.model.factory;
import com.auction.shared.model.item.Vehicle;
import com.auction.shared.model.item.Item;
import com.auction.shared.model.item.VehicleDTO;

import java.util.Map;
import java.math.BigDecimal;

public class VehicleFactory implements ItemFactory<VehicleDTO> {
    @Override
    public Item createItem(VehicleDTO dto) {
        return new Vehicle(dto);
    }
}
