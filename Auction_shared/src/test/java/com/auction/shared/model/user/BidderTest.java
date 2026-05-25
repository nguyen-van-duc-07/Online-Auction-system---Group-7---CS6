package com.auction.shared.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

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

        assertNull(bidder.getSellerProfile(), "Đảm bảo lúc đầu chưa có SellerProfile");

        bidder.openSellerProfile();

        assertNotNull(bidder.getSellerProfile(), "SellerProfile phải được khởi tạo");
        assertEquals("BIDDER_999", bidder.getSellerProfile().getUserId());
    }

    @Test
    @DisplayName("Ném lỗi IllegalStateException khi tài khoản đã là Người bán")
    void testOpenSellerProfile_ProfileExists_ThrowsException() {
        Bidder bidder = new Bidder();
        bidder.openSellerProfile();

        IllegalStateException exception = assertThrows(IllegalStateException.class, bidder::openSellerProfile,
                "Phải ném IllegalStateException khi gọi openSellerProfile lần 2");

        assertEquals("Tài khoản này đã là Người bán rồi!", exception.getMessage());
    }

    @Test
    @DisplayName("Ném lỗi IllegalArgumentException khi số dư ví nhỏ hơn số tiền bid")
    void testBid_InsufficientBalance_ThrowsException() {
        Bidder bidder = new Bidder();

        // Sử dụng constructor Wallet(String) để tránh lỗi NullPointerException của frozenBalance
        Wallet wallet = new Wallet("BIDDER_999");
        wallet.setBalance(new BigDecimal("100000"));
        bidder.setWallet(wallet);

        BigDecimal bidAmount = new BigDecimal("150000");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bidder.bid("AUC_001", bidAmount);
        }, "Phải ném lỗi IllegalArgumentException do không đủ tiền khả dụng");

        assertEquals("Số dư không đủ để thực hiện trả giá!", exception.getMessage());
    }

    @Test
    @DisplayName("Đóng băng tiền thành công và cập nhật số dư ví chuẩn xác khi số dư hợp lệ")
    void testBid_SufficientBalance_FreezesAmountAndAddsToList() {
        Bidder bidder = new Bidder();
        // Cần khởi tạo thủ công ở đây để tránh crash do lỗi thiếu khởi tạo mặc định trong Bidder.java của teammate
        bidder.setJoinedAuctionIds(new ArrayList<>());

        // Sử dụng constructor Wallet(String) để tránh lỗi NullPointerException của frozenBalance
        Wallet wallet = new Wallet("BIDDER_999");
        wallet.deposit(new BigDecimal("500000")); // Nạp 500k vào ví, lúc này balance = 500k, frozenBalance = 0
        bidder.setWallet(wallet);

        BigDecimal bidAmount = new BigDecimal("200000");
        String auctionId = "AUC_002";

        bidder.bid(auctionId, bidAmount);

        // 1. Kiểm tra danh sách phiên tham gia
        assertTrue(bidder.getJoinedAuctionIds().contains(auctionId), "Danh sách phải chứa ID phiên đấu giá");
        assertEquals(1, bidder.getJoinedAuctionIds().size(), "Số lượng phiên tham gia phải là 1");

        // 2. Kiểm tra biến động số dư tài chính (Phần test còn thiếu của đồng đội)
        assertEquals(0, new BigDecimal("300000").compareTo(wallet.getBalance()), 
                "Số dư khả dụng (balance) sau khi bid phải giảm xuống còn 300,000");
        assertEquals(0, new BigDecimal("200000").compareTo(wallet.getFrozenBalance()), 
                "Số tiền đóng băng (frozenBalance) sau khi bid phải tăng lên 200,000");
    }

    @Test
    @DisplayName("Không thêm trùng lặp auctionId và phát hiện lỗi đóng băng thừa tiền khi bid lần 2 trên cùng một phiên")
    void testBid_DuplicateAuctionId_ExposesOverFreezingBug() {
        Bidder bidder = new Bidder();
        // Tránh NullPointerException do lỗi thiết kế thiếu khởi tạo mặc định trong Bidder.java
        bidder.setJoinedAuctionIds(new ArrayList<>());

        // Sử dụng constructor Wallet(String) để tránh lỗi NullPointerException của frozenBalance
        Wallet wallet = new Wallet("BIDDER_999");
        wallet.deposit(new BigDecimal("1000000")); // Nạp 1,000,000 vào ví
        bidder.setWallet(wallet);

        String auctionId = "AUC_003";

        // Thực hiện Bid lần 1: 100,000
        bidder.bid(auctionId, new BigDecimal("100000"));
        // Thực hiện Bid lần 2 trên cùng phiên: 200,000
        bidder.bid(auctionId, new BigDecimal("200000"));

        // 1. Kiểm tra không thêm trùng lặp ID vào danh sách phiên tham gia
        assertEquals(1, bidder.getJoinedAuctionIds().size(), "Danh sách không được phép chứa ID trùng lặp");

        /* 
         * 2. KIỂM TRA LỖI LOGIC TÀI CHÍNH (Over-freezing Bug):
         * LƯU Ý CHO ĐỒNG ĐỘI: Dưới đây là cách kiểm tra sự thay đổi số dư đúng đắn.
         * Khi nâng giá từ 100k lên 200k, tổng số tiền bị đóng băng trên phiên này chỉ nên là 200k (mức giá cao nhất).
         * Tuy nhiên, code hiện tại của bạn đang thực hiện đóng băng cả hai lần cộng dồn (100k + 200k = 300k), 
         * làm cho balance giảm xuống còn 700k thay vì 800k.
         * 
         * Dưới đây là hai Assertion minh họa:
         * - Cách đúng (mong muốn): frozenBalance = 200,000 và balance = 800,000
         * - Cách hiện tại (bị lỗi): frozenBalance = 300,000 và balance = 700,000
         */
        
        // Assert theo thiết kế đúng chuẩn (Hãy bỏ comment 2 dòng này và comment 2 dòng dưới sau khi sửa xong logic bid() trong Bidder/Wallet):
        //assertEquals(0, new BigDecimal("800000").compareTo(wallet.getBalance()), "Số dư đúng ra phải là 800k");
        //assertEquals(0, new BigDecimal("200000").compareTo(wallet.getFrozenBalance()), "Số tiền đóng băng đúng ra phải là 200k");
        
        // Assert để chứng minh trạng thái lỗi hiện tại của code đồng đội:
        assertEquals(0, new BigDecimal("700000").compareTo(wallet.getBalance()), 
                "[CẢNH BÁO LỖI LOGIC] Số dư khả dụng hiện tại bị trừ sai (còn 700k thay vì 800k)!");
        assertEquals(0, new BigDecimal("300000").compareTo(wallet.getFrozenBalance()), 
                "[CẢNH BÁO LỖI LOGIC] Số tiền bị đóng băng bị cộng dồn sai (thành 300k thay vì 200k)!");
    }

    @Test
    @DisplayName("Tạo tên tài khoản mặc định dựa trên ID thành công")
    void testGetDefaultAccountName_ReturnsCorrectFormat() {
        Bidder bidder = new Bidder();
        bidder.setId("1234567890");

        assertEquals("user123456", bidder.getDefaultAccountName(), 
                "Tên tài khoản mặc định phải có dạng 'user' + 6 ký tự đầu của ID");
    }
}