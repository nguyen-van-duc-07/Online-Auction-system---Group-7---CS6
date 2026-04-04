package com.example.auctionserver.factory;

import com.example.auctionserver.dto.ItemDTO;
import com.example.auctionserver.model.Art;
import com.example.auctionserver.model.Item;

public class ArtFactory extends ItemFactory{
    @Override
    protected Item createItem() {
        return new Art();
    }

    @Override
    protected void setupItem(Item item, ItemDTO dto) {
        Art art = (Art) item;

        // Lấy thẳng dữ liệu từ DTO đắp vào Object
        art.setArtistName(dto.getArtistName());
        art.setMaterial(dto.getMaterial());
        art.setHasCertificate(dto.isHasCertificate());
    }
}
