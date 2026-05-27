package com.auction.shared.util;
import com.auction.shared.enums.NotificationType;
import com.auction.shared.model.notification.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTemplateTest {

    private final String dummyUserId = "USER_123";
    private final String dummyItemName = "iPhone 15 Pro Max";
    private final String dummyOrderId = "ORDER_999";
    private final String dummyAuctionId = "AUCTION_777";
    private final BigDecimal dummyPrice = new BigDecimal("25000000.00");

    @Test
    @DisplayName("auctionWon() tạo thông báo thắng đấu giá chính xác")
    void testAuctionWon_ShouldCreateValidNotification() {
        Notification notification = NotificationTemplate.auctionWon(dummyUserId, dummyItemName, dummyPrice, dummyOrderId);
        
        assertNotNull(notification);
        assertEquals(dummyUserId, notification.getUserId());
        assertEquals(NotificationType.AUCTION_WON, notification.getType());
        assertEquals("Chúc mừng! Bạn đã thắng phiên đấu giá", notification.getTitle());
        assertTrue(notification.getContent().contains(dummyItemName));
        assertTrue(notification.getContent().contains("25.000.000 đ") || notification.getContent().contains("25,000,000 đ"));
        assertEquals(dummyOrderId, notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("auctionEndedWithWinner() tạo thông báo đấu giá kết thúc có người thắng cho người bán")
    void testAuctionEndedWithWinner_ShouldCreateValidNotification() {
        Notification notification = NotificationTemplate.auctionEndedWithWinner("SELLER_123", dummyItemName, dummyPrice, dummyOrderId);
        
        assertNotNull(notification);
        assertEquals("SELLER_123", notification.getUserId());
        assertEquals(NotificationType.AUCTION_ENDED, notification.getType());
        assertEquals("Phiên đấu giá của bạn đã kết thúc", notification.getTitle());
        assertTrue(notification.getContent().contains(dummyItemName));
        assertTrue(notification.getContent().contains("25.000.000 đ") || notification.getContent().contains("25,000,000 đ"));
        assertEquals(dummyOrderId, notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("auctionEndedNoWinner() tạo thông báo đấu giá không ai mua cho người bán")
    void testAuctionEndedNoWinner_ShouldCreateValidNotification() {
        Notification notification = NotificationTemplate.auctionEndedNoWinner("SELLER_123", dummyItemName, dummyAuctionId);
        
        assertNotNull(notification);
        assertEquals("SELLER_123", notification.getUserId());
        assertEquals(NotificationType.AUCTION_ENDED, notification.getType());
        assertEquals("Phiên đấu giá kết thúc không có người mua", notification.getTitle());
        assertTrue(notification.getContent().contains(dummyItemName));
        assertEquals(dummyAuctionId, notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("orderConfirmedForSeller() tạo thông báo thanh toán thành công cho người bán")
    void testOrderConfirmedForSeller_ShouldCreateValidNotification() {
        Notification notification = NotificationTemplate.orderConfirmedForSeller("SELLER_123", dummyItemName, dummyPrice, dummyOrderId);
        
        assertNotNull(notification);
        assertEquals("SELLER_123", notification.getUserId());
        assertEquals(NotificationType.ORDER_CONFIRMED, notification.getType());
        assertEquals("Đơn hàng đã được xác nhận thanh toán", notification.getTitle());
        assertTrue(notification.getContent().contains(dummyItemName));
        assertEquals(dummyOrderId, notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("orderConfirmedForBuyer() tạo thông báo thanh toán thành công cho người mua")
    void testOrderConfirmedForBuyer_ShouldCreateValidNotification() {
        Notification notification = NotificationTemplate.orderConfirmedForBuyer(dummyUserId, dummyItemName, dummyPrice, dummyOrderId);
        
        assertNotNull(notification);
        assertEquals(dummyUserId, notification.getUserId());
        assertEquals(NotificationType.ORDER_CONFIRMED, notification.getType());
        assertEquals("Thanh toán thành công", notification.getTitle());
        assertTrue(notification.getContent().contains(dummyItemName));
        assertEquals(dummyOrderId, notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("orderCancelledForSeller() tạo thông báo người mua hủy đơn cho người bán")
    void testOrderCancelledForSeller_ShouldCreateValidNotification() {
        BigDecimal penalty = new BigDecimal("2500000.00");
        Notification notification = NotificationTemplate.orderCancelledForSeller("SELLER_123", dummyItemName, penalty, dummyOrderId);
        
        assertNotNull(notification);
        assertEquals("SELLER_123", notification.getUserId());
        assertEquals(NotificationType.ORDER_CANCELLED_BY_BUYER, notification.getType());
        assertEquals("Người mua đã hủy đơn hàng", notification.getTitle());
        assertTrue(notification.getContent().contains(dummyItemName));
        assertTrue(notification.getContent().contains("2.500.000 đ") || notification.getContent().contains("2,500,000 đ"));
        assertEquals(dummyOrderId, notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("orderCancelledForBuyer() tạo thông báo hủy đơn cho người mua")
    void testOrderCancelledForBuyer_ShouldCreateValidNotification() {
        BigDecimal penalty = new BigDecimal("2500000.00");
        Notification notification = NotificationTemplate.orderCancelledForBuyer(dummyUserId, dummyItemName, penalty, dummyOrderId);
        
        assertNotNull(notification);
        assertEquals(dummyUserId, notification.getUserId());
        assertEquals(NotificationType.ORDER_CANCELLED, notification.getType());
        assertEquals("Bạn đã hủy đơn hàng", notification.getTitle());
        assertTrue(notification.getContent().contains(dummyItemName));
        assertEquals(dummyOrderId, notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("orderExpiredForBuyer() tạo thông báo đơn hàng quá hạn cho người mua")
    void testOrderExpiredForBuyer_ShouldCreateValidNotification() {
        BigDecimal penalty = new BigDecimal("2500000.00");
        Notification notification = NotificationTemplate.orderExpiredForBuyer(dummyUserId, dummyItemName, penalty, dummyOrderId);
        
        assertNotNull(notification);
        assertEquals(dummyUserId, notification.getUserId());
        assertEquals(NotificationType.ORDER_CANCELLED, notification.getType());
        assertEquals("Đơn hàng đã bị hủy do quá hạn", notification.getTitle());
        assertTrue(notification.getContent().contains(dummyItemName));
        assertEquals(dummyOrderId, notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("orderExpiredForSeller() tạo thông báo đơn hàng quá hạn cho người bán")
    void testOrderExpiredForSeller_ShouldCreateValidNotification() {
        BigDecimal penalty = new BigDecimal("2500000.00");
        Notification notification = NotificationTemplate.orderExpiredForSeller("SELLER_123", dummyItemName, penalty, dummyOrderId);
        
        assertNotNull(notification);
        assertEquals("SELLER_123", notification.getUserId());
        assertEquals(NotificationType.ORDER_CANCELLED, notification.getType());
        assertEquals("Đơn hàng đã bị hủy do quá hạn", notification.getTitle());
        assertTrue(notification.getContent().contains(dummyItemName));
        assertEquals(dummyOrderId, notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("sellerApproved() tạo thông báo phê duyệt tài khoản người bán thành công")
    void testSellerApproved_ShouldCreateValidNotification() {
        Notification notification = NotificationTemplate.sellerApproved(dummyUserId);
        
        assertNotNull(notification);
        assertEquals(dummyUserId, notification.getUserId());
        assertEquals(NotificationType.SELLER_APPROVED, notification.getType());
        assertEquals("Tài khoản người bán đã được duyệt", notification.getTitle());
        assertNull(notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("sellerRejected() tạo thông báo từ chối tài khoản người bán")
    void testSellerRejected_ShouldCreateValidNotification() {
        Notification notification = NotificationTemplate.sellerRejected(dummyUserId);
        
        assertNotNull(notification);
        assertEquals(dummyUserId, notification.getUserId());
        assertEquals(NotificationType.SELLER_REJECTED, notification.getType());
        assertEquals("Tài khoản người bán bị từ chối", notification.getTitle());
        assertNull(notification.getReferenceId());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("welcome() tạo thông báo chào mừng thành viên mới")
    void testWelcome_ShouldCreateValidNotification() {
        Notification notification = NotificationTemplate.welcome(dummyUserId);
        
        assertNotNull(notification);
        assertEquals(dummyUserId, notification.getUserId());
        assertEquals(NotificationType.SYSTEM, notification.getType());
        assertEquals("Chào mừng bạn đến với Đấu Giá 88!", notification.getTitle());
        assertNull(notification.getReferenceId());
        assertFalse(notification.isRead());
    }
}
