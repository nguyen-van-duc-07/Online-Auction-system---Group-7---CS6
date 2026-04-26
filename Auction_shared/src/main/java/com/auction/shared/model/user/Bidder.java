package com.auction.shared.model.user;

import com.auction.shared.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Bidder extends User{
    private Wallet wallet;
    private List<String> joinedAuctionIds;
    private SellerProfile sellerProfile;

    public Bidder() {
    }

    public Bidder(String userName, String password, String email, LocalDate dob, String phoneNumber, String address, UserRole role, Wallet wallet, List<String> joinedAuctionIds, SellerProfile sellerProfile) {
        super(userName, password, email, dob, phoneNumber, address, role);
        this.wallet = wallet;
        this.joinedAuctionIds = joinedAuctionIds;
        this.sellerProfile = sellerProfile;
    }

    public void openSellerProfile() {
        if (this.sellerProfile != null) {
            throw new IllegalStateException("Tài khoản này đã là Người bán rồi!");
        }
        this.sellerProfile = new SellerProfile(this.id);
    }
    public void bid(String auctionId, BigDecimal amount){
        this.wallet.freeze(amount);
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public List<String> getJoinedAuctionIds() {
        return joinedAuctionIds;
    }

    public void setJoinedAuctionIds(List<String> joinedAuctionIds) {
        this.joinedAuctionIds = joinedAuctionIds;
    }

    public SellerProfile getSellerProfile() {
        return sellerProfile;
    }

    public void setSellerProfile(SellerProfile sellerProfile) {
        this.sellerProfile = sellerProfile;
    }
}
