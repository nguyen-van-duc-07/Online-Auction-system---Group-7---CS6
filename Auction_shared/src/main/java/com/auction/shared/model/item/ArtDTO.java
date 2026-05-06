package com.auction.shared.model.item;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder // Bắt buộc phải có để kế thừa khả năng build từ ItemDTO
public class ArtDTO extends ItemDTO {
    private String artistName;
    private String material;
    private int yearCreated;
    private boolean hasCertificate;
}