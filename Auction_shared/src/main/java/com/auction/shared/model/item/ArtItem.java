package com.auction.shared.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ArtItem extends Item {
    private String artist;
    private String certificate;
    private String year;
    private String dimension;
}
