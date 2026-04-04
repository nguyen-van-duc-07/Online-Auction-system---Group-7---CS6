package com.example.auctionserver.factory;

import com.example.auctionserver.model.Item;
import com.example.auctionserver.model.Vehicle;
import com.example.auctionserver.dto.ItemDTO;

public class VehicleFactory extends ItemFactory {

    @Override
    protected Item createItem() {
        return new Vehicle(); // Nhớ thêm public Vehicle(){} bên model nhé
    }

    @Override
    protected void setupItem(Item item, ItemDTO dto) {
        Vehicle vehicle = (Vehicle) item;

        // Map dữ liệu từ DTO sang Vehicle
        vehicle.setBrand(dto.getBrand());
        vehicle.setMileage(dto.getMileage());
        vehicle.setEngineType(dto.getEngineType());
    }
}