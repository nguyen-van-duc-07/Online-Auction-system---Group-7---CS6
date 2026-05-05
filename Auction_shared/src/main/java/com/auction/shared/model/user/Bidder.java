package com.auction.shared.model.user;

import com.auction.shared.enums.UserRole;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Bidder extends User {

  private Wallet wallet;
  private SellerProfile sellerProfile;

  public Bidder() {
  }

  public Bidder(String userName, String password) {
    super(userName, password);
  }

  public Bidder(String id,
                LocalDateTime createdAt,
                String userName,
                String password) {
    super(id, createdAt, userName, password);
  }

  public Bidder(String userName,
                String password,
                String email,
                LocalDate dob,
                String phoneNumber,
                String address,
                UserRole role,
                Wallet wallet,
                SellerProfile sellerProfile) {

    super(userName, password, email, dob, phoneNumber, address, role);
    this.wallet = wallet;
    this.sellerProfile = sellerProfile;
  }

  public void openSellerProfile() {
    if (this.sellerProfile != null) {
      throw new IllegalStateException("Tài khoản này đã là người bán rồi!");
    }
    this.sellerProfile = new SellerProfile(this.id);
  }

  // GETTERS / SETTERS

  public Wallet getWallet() {
    return wallet;
  }

  public void setWallet(Wallet wallet) {
    this.wallet = wallet;
  }

  public SellerProfile getSellerProfile() {
    return sellerProfile;
  }

  public void setSellerProfile(SellerProfile sellerProfile) {
    this.sellerProfile = sellerProfile;
  }
}
