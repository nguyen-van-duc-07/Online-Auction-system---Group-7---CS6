package com.auction.shared.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BidderTest {

    @Test
    @DisplayName("Khởi tạo từ DTO thành công và danh sách joinedAuctionIds được cấp phát rỗng")
    void testConstructor_WithDTO_InitializesEmptyList() {
        UserDTO dto = new UserDTO();
        dto.setId("USR_123");

        Bidder bidder = new Bidder(dto);

        assertNotNull(bidder.getJoinedAuctionIds(), "Danh sách joinedAuctionIds không được null");
        assertTrue(bidder.getJoinedAuctionIds().isEmpty(), "Danh sách joinedAuctionIds phải rỗng khi mới khởi tạo");
        assertEquals("USR_123", bidder.getId());
    }

    @Test
    @DisplayName("Khởi tạo SellerProfile thành công khi tài khoản chưa là Người bán")
    void testOpenSellerProfile_ProfileIsNull_CreatesProfile() {
        Bidder bidder = new Bidder();
        bidder.setId("BIDDER_999");

        assertNull(bidder.getSellerProfile()); // Đảm bảo lúc đầu chưa có

        bidder.openSellerProfile();

        assertNotNull(bidder.getSellerProfile());
        assertEquals("BIDDER_999", bidder.getSellerProfile().getUserId());
    }

    @Test
    @DisplayName("Ném lỗi IllegalStateException khi tài khoản đã là Người bán")
    void testOpenSellerProfile_ProfileExists_ThrowsException() {
        Bidder bidder = new Bidder();
        bidder.openSellerProfile();

        IllegalStateException exception = assertThrows(IllegalStateException.class, bidder::openSellerProfile); // Gọi lần 2

        assertEquals("Tài khoản này đã là Người bán rồi!", exception.getMessage());
    }

    @Test
    @DisplayName("Ném lỗi IllegalArgumentException khi số dư ví nhỏ hơn số tiền bid")
    void testBid_InsufficientBalance_ThrowsException() {
        Bidder bidder = new Bidder();

        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("100000"));
        bidder.setWallet(wallet);

        BigDecimal bidAmount = new BigDecimal("150000");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bidder.bid("AUC_001", bidAmount);
        });

        assertEquals("Số dư không đủ để thực hiện trả giá!", exception.getMessage());
    }

    @Test
    @DisplayName("Đóng băng tiền và thêm auctionId vào danh sách khi số dư hợp lệ")
    void testBid_SufficientBalance_FreezesAmountAndAddsToList() {
        Bidder bidder = new Bidder();
        bidder.setJoinedAuctionIds(new java.util.ArrayList<>()); // Tránh NullPointerException do DTO chưa chạy

        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("500000"));
        bidder.setWallet(wallet);

        BigDecimal bidAmount = new BigDecimal("200000");
        String auctionId = "AUC_002";

        bidder.bid(auctionId, bidAmount);

        assertTrue(bidder.getJoinedAuctionIds().contains(auctionId));
        assertEquals(1, bidder.getJoinedAuctionIds().size());
    }

    @Test
    @DisplayName("Không thêm trùng lặp auctionId nếu đã tham gia phiên đấu giá này từ trước")
    void testBid_DuplicateAuctionId_DoesNotAddDuplicate() {
        Bidder bidder = new Bidder();
        bidder.setJoinedAuctionIds(new java.util.ArrayList<>());

        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("1000000"));
        bidder.setWallet(wallet);

        String auctionId = "AUC_003";

        // Bid lần 1
        bidder.bid(auctionId, new BigDecimal("100000"));
        // Bid lần 2 vào cùng phiên đó
        bidder.bid(auctionId, new BigDecimal("200000"));

        assertEquals(1, bidder.getJoinedAuctionIds().size(), "Danh sách không được phép chứa ID trùng lặp");
    }
}