package com.auction.shared.model;

import java.util.UUID;

public abstract class Entity {
    // Tính đóng gói (Encapsulation): protected để các lớp con có thể truy cập
    protected String id;

    public Entity() {
        // Tự động tạo ID ngẫu nhiên khi khởi tạo đối tượng
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
