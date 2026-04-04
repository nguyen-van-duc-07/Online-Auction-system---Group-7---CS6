package com.example.auctionserver.model;

import java.io.Serializable;
import java.time.LocalDateTime;


abstract public class Entity implements Serializable {
    protected String id;
    protected LocalDateTime createdAt;

    public Entity(){}
    public Entity(String id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
