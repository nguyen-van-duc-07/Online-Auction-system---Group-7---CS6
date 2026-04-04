package com.example.auctionserver.model;

import java.time.LocalDate;

public class Admin extends User{
    public Admin() {
    }

    public Admin(String userName, String password, String email, LocalDate dob, String phoneNumber) {
        super(userName, password, email, dob, phoneNumber, UserRole.ADMIN);
    }

}