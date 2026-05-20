package com.auction.shared.model.auction;
import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.item.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    private Item dummyItem;

    @BeforeEach
    void setUp() {
        // Mock một Item rỗng hoặc khởi tạo cơ bản vì logic Auction hiện không phụ thuộc sâu vào Item
        dummyItem = new Item();
    }

    // ==========================================
    // 1. TEST CONSTRUCTOR LẬP LỊCH THỜI GIAN
    // ==========================================

    @Test
    @DisplayName("Khởi tạo Auction với startTime ở quá khứ/hiện tại -> Trạng thái ACTIVE")
    void testConstructor_StartTimeInPast_ShouldBeActive() {
        LocalDateTime pastStartTime = LocalDateTime.now().minusMinutes(10);
        LocalDateTime futureEndTime = LocalDateTime.now().plusDays(1);

        Auction auction = new Auction(dummyItem, new BigDecimal("100.00"), pastStartTime, futureEndTime);

        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
    }

    @Test
    @DisplayName("Khởi tạo Auction với startTime ở tương lai -> Trạng thái WAITING")
    void testConstructor_StartTimeInFuture_ShouldBeWaiting() {
        LocalDateTime futureStartTime = LocalDateTime.now().plusMinutes(10);
        LocalDateTime futureEndTime = LocalDateTime.now().plusDays(1);

        Auction auction = new Auction(dummyItem, new BigDecimal("100.00"), futureStartTime, futureEndTime);

        assertEquals(AuctionStatus.WAITING, auction.getStatus());
    }

    // ==========================================
    // 2. TEST CHUYỂN ĐỔI TRẠNG THÁI (STATE MACHINE)
    // ==========================================

    @Test
    @DisplayName("Hàm start() chuyển từ WAITING sang ACTIVE")
    void testStart_FromWaiting_ShouldChangeToActive() {
        Auction auction = new Auction(dummyItem, new BigDecimal("100"), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        assertEquals(AuctionStatus.WAITING, auction.getStatus());

        auction.start();

        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
    }

    @Test
    @DisplayName("Hàm start() bỏ qua nếu trạng thái không phải WAITING")
    void testStart_FromClosed_ShouldDoNothing() {
        Auction auction = new Auction(dummyItem, new BigDecimal("100"), LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2));
        auction.close(); // Chuyển sang CLOSED

        auction.start(); // Cố tình start lại

        assertEquals(AuctionStatus.CLOSED, auction.getStatus()); // Vẫn phải là CLOSED
    }

    @Test
    @DisplayName("Hàm close() chuyển từ ACTIVE sang CLOSED")
    void testClose_FromActive_ShouldChangeToClosed() {
        Auction auction = new Auction(dummyItem, new BigDecimal("100"), LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusDays(1));

        auction.close();

        assertEquals(AuctionStatus.CLOSED, auction.getStatus());
    }

    @Test
    @DisplayName("Hàm cancel() ném lỗi IllegalStateException nếu đã CANCELED trước đó")
    void testCancel_AlreadyCanceled_ShouldThrowException() {
        Auction auction = new Auction(dummyItem, new BigDecimal("100"), LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        auction.cancel(); // Lần 1
        assertEquals(AuctionStatus.CANCELED, auction.getStatus());

        // Lần 2 phải ném lỗi
        assertThrows(IllegalStateException.class, auction::cancel);
    }

    // ==========================================
    // 3. TEST KIỂM TRA HẠN GIỜ (IS EXPIRED)
    // ==========================================

    @Test
    @DisplayName("isExpired() trả về true nếu đã qua endTime")
    void testIsExpired_PastEndTime_ShouldReturnTrue() {
        Auction auction = new Auction(dummyItem, new BigDecimal("100"), LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        assertTrue(auction.isExpired());
    }

    @Test
    @DisplayName("isExpired() trả về false nếu chưa tới endTime hoặc endTime null")
    void testIsExpired_FutureEndTimeOrNull_ShouldReturnFalse() {
        Auction auction1 = new Auction(dummyItem, new BigDecimal("100"), LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertFalse(auction1.isExpired());

        Auction auction2 = new Auction(dummyItem, new BigDecimal("100"), LocalDateTime.now(), null);
        assertFalse(auction2.isExpired());
    }

    // ==========================================
    // 4. TEST LOGIC ĐẶT GIÁ (APPLY BID)
    // ==========================================

    @Test
    @DisplayName("applyBid() thành công: cập nhật giá, người bid và lưu lịch sử")
    void testApplyBid_ValidBid_ShouldSucceed() {
        Auction auction = new Auction(dummyItem, new BigDecimal("100.00"), LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusDays(1));
        auction.setId("AUCTION_123");
        boolean result = auction.applyBid("USER_123", new BigDecimal("150.00"));

        assertTrue(result);
        assertEquals(new BigDecimal("150.00"), auction.getCurrentHighestPrice());
        assertEquals("USER_123", auction.getHighestBidderId());
        assertEquals(1, auction.getBidHistory().size());
        assertEquals(new BigDecimal("150.00"), auction.getBidHistory().get(0).getBidAmount());
    }

    @Test
    @DisplayName("applyBid() thất bại do giá đưa ra <= giá hiện tại")
    void testApplyBid_LowerOrEqualAmount_ShouldFail() {
        Auction auction = new Auction(dummyItem, new BigDecimal("100.00"), LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusDays(1));

        boolean resultEqual = auction.applyBid("USER_123", new BigDecimal("100.00"));
        boolean resultLower = auction.applyBid("USER_456", new BigDecimal("50.00"));

        assertFalse(resultEqual);
        assertFalse(resultLower);
        assertEquals(new BigDecimal("100.00"), auction.getCurrentHighestPrice()); // Giá không đổi
        assertTrue(auction.getBidHistory().isEmpty()); // Không có giao dịch nào được lưu
    }

    @Test
    @DisplayName("applyBid() thất bại do phiên đấu giá không ở trạng thái ACTIVE")
    void testApplyBid_NotActiveAuction_ShouldFail() {
        // Đang WAITING
        Auction waitingAuction = new Auction(dummyItem, new BigDecimal("100.00"), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        boolean result1 = waitingAuction.applyBid("USER_123", new BigDecimal("150.00"));

        // Đã CLOSED
        Auction closedAuction = new Auction(dummyItem, new BigDecimal("100.00"), LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        closedAuction.close();
        boolean result2 = closedAuction.applyBid("USER_123", new BigDecimal("150.00"));

        assertFalse(result1);
        assertFalse(result2);
    }
}