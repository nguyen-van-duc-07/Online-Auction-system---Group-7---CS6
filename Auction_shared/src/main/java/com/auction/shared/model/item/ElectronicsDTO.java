package com.auction.shared.model.item;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder // Bắt buộc phải có để kế thừa khả năng build từ ItemDTO
public class ElectronicsDTO extends ItemDTO {
    private String brand;
    private int warrantyMonths;
    private String condition;
}