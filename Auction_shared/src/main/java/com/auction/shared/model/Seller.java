package com.auction.shared.model;

public class Seller extends User {
    private double sellerRating; // Seller cần có điểm đánh giá uy tín

    public Seller(String username, String password) {
        super(username, password, "Seller");
        this.sellerRating = 5.0; // Điểm uy tín mặc định lúc mới đăng ký
    }

    public double getSellerRating() { return sellerRating; }
    public void setSellerRating(double sellerRating) { this.sellerRating = sellerRating; }

    // Ghi đè phương thức từ lớp cha (Polymorphism)
    @Override
    public void printRoleInfo() {
        System.out.println("Tôi là Seller: " + this.username + ", Uy tín: " + this.sellerRating + " sao");
    }
}
