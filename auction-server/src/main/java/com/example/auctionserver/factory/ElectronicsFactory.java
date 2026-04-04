package com.example.auctionserver.factory;

import com.example.auctionserver.model.Item;
import com.example.auctionserver.model.Electronics;
import com.example.auctionserver.dto.ItemDTO;

public class ElectronicsFactory extends ItemFactory {

    @Override
    protected Item createItem() {
        return new Electronics(); // Nhớ thêm public Electronics(){} bên model nhé
    }

    @Override
    protected void setupItem(Item item, ItemDTO dto) {
        Electronics electronics = (Electronics) item;

        // Map dữ liệu từ DTO sang Electronics
        electronics.setBrand(dto.getBrand());
        electronics.setWarrantyMonths(dto.getWarrantyMonths());
        electronics.setWeight(dto.getWeight());
    }
}