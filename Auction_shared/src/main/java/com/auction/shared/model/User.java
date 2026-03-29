package com.auction.shared.model;

public abstract class User extends Entity {
    protected String username;
    protected String password;
    protected String role;

    public User(String username, String password, String role) {
        super(); // Gọi constructor của Entity để tạo ID
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // --- Tính đa hình (Polymorphism) ---
    // Phương thức trừu tượng này bắt buộc các lớp con (Bidder, Seller) phải tự định nghĩa lại (override)
    public abstract void printRoleInfo();

    // --- Tính đóng gói (Getters / Setters) ---
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
