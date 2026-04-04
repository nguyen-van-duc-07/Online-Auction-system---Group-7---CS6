package com.example.auctionserver.model;

import java.time.LocalDateTime;
import java.util.UUID;

abstract public class Item extends Entity {
    protected String name, description;
    protected int yearCreated;

    public Item(){}
    public Item(String name, String description, int yearCreated) {
        super();
        this.name = name;
        this.description = description;
        this.yearCreated = yearCreated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getYearCreated() {
        return yearCreated;
    }

    public void setYearCreated(int yearCreated) {
        this.yearCreated = yearCreated;
    }
}
