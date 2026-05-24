package com.auction.shared.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FashionItem extends Item {
    private String brand;
    private String size;
    private String material;
    private String gender;
}
