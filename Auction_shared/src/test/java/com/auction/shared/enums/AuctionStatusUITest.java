package com.auction.shared.enums;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuctionStatusUITest {

    @Test
    @DisplayName("Ánh xạ thành công từ trạng thái WAITING sang sắp diễn ra")
    void testFromShared_WaitingStatus_ShouldMapToWaitingUI() {
        AuctionStatusUI uiStatus = AuctionStatusUI.fromShared(AuctionStatus.WAITING);
        
        assertEquals(AuctionStatusUI.WAITING, uiStatus);
        assertEquals("Sắp diễn ra", uiStatus.getDisplayName());
        assertEquals("#f39c12", uiStatus.getColor());
    }

    @Test
    @DisplayName("Ánh xạ thành công từ trạng thái ACTIVE sang đang diễn ra")
    void testFromShared_ActiveStatus_ShouldMapToActiveUI() {
        AuctionStatusUI uiStatus = AuctionStatusUI.fromShared(AuctionStatus.ACTIVE);
        
        assertEquals(AuctionStatusUI.ACTIVE, uiStatus);
        assertEquals("Đang diễn ra", uiStatus.getDisplayName());
        assertEquals("#27ae60", uiStatus.getColor());
    }

    @Test
    @DisplayName("Ánh xạ thành công từ trạng thái CLOSED sang đã kết thúc")
    void testFromShared_ClosedStatus_ShouldMapToClosedUI() {
        AuctionStatusUI uiStatus = AuctionStatusUI.fromShared(AuctionStatus.CLOSED);
        
        assertEquals(AuctionStatusUI.CLOSED, uiStatus);
        assertEquals("Đã kết thúc", uiStatus.getDisplayName());
        assertEquals("#7f8c8d", uiStatus.getColor());
    }

    @Test
    @DisplayName("Ném lỗi IllegalArgumentException khi ánh xạ từ trạng thái CANCELED (không được hỗ trợ ở giao diện)")
    void testFromShared_CanceledStatus_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> AuctionStatusUI.fromShared(AuctionStatus.CANCELED));
    }
}
