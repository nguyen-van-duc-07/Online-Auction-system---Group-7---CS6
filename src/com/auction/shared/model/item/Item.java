package com.auction.shared.model.item;
import com.auction.shared.model.core.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Item extends Entity {
    protected String name, category, description, sellerId, imageUrl;
    protected BigDecimal basePrice;
    /*Thứ tự nhập vào:
    name
    category
    description
    sellerId
    imageUrl
    basePrice
     */
    //Constructor dùng cho tạo mới sản phẩm từ giao diện người bán.
    public Item(String name, String category, String description, String sellerId, String imageUrl, BigDecimal basePrice){
        super();
        this.name = name;
        this.category = category;
        this.description = description;
        this.sellerId = sellerId;
        this.imageUrl = imageUrl;
        this.basePrice = basePrice;
    }
    //Constructor dùng cho ItemDAO khi đọc dữ liệu từ MySQL.
    public Item(String id, LocalDateTime createdAt, String name, String category, String description, String sellerId, String imageUrl, BigDecimal basePrice){
        super(id, createdAt);
        this.name = name;
        this.category = category;
        this.description = description;
        this.sellerId = sellerId;
        this.imageUrl = imageUrl;
        this.basePrice = basePrice;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * Phương thức trừu tượng để thể hiện tính Đa hình (Polymorphism).
     * Các lớp con (Electronics, Art, Vehicle) BẮT BUỘC phải ghi đè (override) hàm này
     * để hiển thị thông tin đặc thù của riêng chúng trên giao diện JavaFX.
     */
    public abstract String getSpecificDetails();
}
