package com.auction.shared.model;

public class Admin extends User {
    private String adminLevel; // Cấp độ quyền hạn

    public Admin(String username, String password, String adminLevel) {
        super(username, password, "Admin");
        this.adminLevel = adminLevel;
    }

    public String getAdminLevel() { return adminLevel; }

    @Override
    public void printRoleInfo() {
        System.out.println("Tôi là Admin: " + this.username + ", Cấp: " + this.adminLevel);
    }
}