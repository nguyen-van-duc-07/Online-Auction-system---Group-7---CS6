package com.auction.shared.model;

public class Bidder extends User {
    private double walletBalance; // Bidder cần có số dư ví để tham gia đấu giá

    public Bidder(String username, String password) {
        super(username, password, "Bidder");
        this.walletBalance = 0.0; // Số dư mặc định
    }

    public double getWalletBalance() { return walletBalance; }
    public void deposit(double amount) { this.walletBalance += amount; }

    // Ghi đè phương thức từ lớp cha (Polymorphism)
    @Override
    public void printRoleInfo() {
        System.out.println("Tôi là Bidder: " + this.username + ", Số dư: " + this.walletBalance);
    }
}
