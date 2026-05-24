package com.auction.shared.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SportItem extends Item {
    private String sportType;
    private String brand;
    private String weightSize;
}
