package com.auction.shared.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.auction.shared.enums.ItemType;

@Getter
@Setter
@NoArgsConstructor
public class ArtItem extends Item {
    private String artist;
    private String certificate;
    private String year;
    private String dimension;

    public ArtItem(String name, String description, java.util.Map<String, String> attributes) {
        super(name, ItemType.ARTS, description, attributes);
        if (attributes != null) {
            this.artist = attributes.get("artist");
            this.certificate = attributes.get("certificate");
            this.year = attributes.get("year");
            this.dimension = attributes.get("dimension");
        }
    }

    @Override
    public String getDisplayCategory() {
        return "Tác phẩm nghệ thuật";
    }

    @Override
    public String printInfo() {
        return "Tác giả: " + (artist != null ? artist : "N/A") +
               " | Năm: " + (year != null ? year : "N/A") +
               " | Kích thước: " + (dimension != null ? dimension : "N/A");
    }
}
