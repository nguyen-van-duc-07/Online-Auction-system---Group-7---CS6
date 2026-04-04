package com.example.auctionserver.model;

public class Art extends Item{
    private String artistName;
    private String material;
    private boolean hasCertificate;
    public Art(){}
    public Art(String name, String description, int yearCreated, String artistName, String material, boolean hasCertificate) {
        super(name, description, yearCreated);
        this.artistName = artistName;
        this.material = material;
        this.hasCertificate = hasCertificate;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public boolean isHasCertificate() {
        return hasCertificate;
    }

    public void setHasCertificate(boolean hasCertificate) {
        this.hasCertificate = hasCertificate;
    }
}
