package com.auction.shared.model.item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Art extends Item{
    private String artistName, material;
    private int yearCreated;
    private boolean hasCertificate;
    //Constructor dùng cho tạo mới sản phẩm từ giao diện người bán.
    public Art(String name, String category, String description,
                       String sellerId, String imageUrl, BigDecimal basePrice,
                       String artistName, String material, int yearCreated, boolean hasCertificate){
        super(name, "Art", description, sellerId, imageUrl, basePrice);
        this.artistName = artistName;
        this.material = material;
        this.yearCreated = yearCreated;
        this.hasCertificate = hasCertificate;
    }
    //Constructor dùng cho ItemDAO khi đọc dữ liệu từ MySQL.
    public Art(String id, LocalDateTime createdAt, String name, String category, String description,
               String sellerId, String imageUrl, BigDecimal basePrice,
               String artistName, String material, int yearCreated, boolean hasCertificate){
        super(id, createdAt, name, "Art", description, sellerId, imageUrl, basePrice);
        this.artistName = artistName;
        this.material = material;
        this.yearCreated = yearCreated;
        this.hasCertificate = hasCertificate;
    }
    @Override
    public String getSpecificDetails() {
        return String.format("Tác giả: %s | Chất liệu: %s | Năm ra mắt: %d | Chứng nhận: %s",
                artistName, material, yearCreated, hasCertificate ? "Có" : "Không");
    }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public int getYearCreated() { return yearCreated; }
    public void setYearCreated(int yearCreated) { this.yearCreated = yearCreated; }

    public boolean HasCertificate() { return hasCertificate; }
    public void setHasCertificate(boolean hasCertificate) { this.hasCertificate = hasCertificate; }
}
