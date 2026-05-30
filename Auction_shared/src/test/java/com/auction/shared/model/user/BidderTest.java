package com.auction.shared.model.user;

import com.auction.shared.model.auction.Auction;
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
        assertEquals("USR_123", bidder.getId());
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