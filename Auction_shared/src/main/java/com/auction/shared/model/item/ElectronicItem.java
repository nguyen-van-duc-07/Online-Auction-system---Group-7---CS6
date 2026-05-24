package com.auction.shared.model.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ElectronicItem extends Item {
    private String model;
    private String brand;
    private String warranty;
    private String condition;
}
