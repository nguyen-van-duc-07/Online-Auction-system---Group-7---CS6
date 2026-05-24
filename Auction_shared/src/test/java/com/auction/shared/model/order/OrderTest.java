package com.auction.shared.model.order;

import com.auction.shared.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("Tạo Order thành công với dữ liệu hợp lệ")
    void testConstructor_FullParams_AssignsAllFields() {
        LocalDateTime expectedResolvedAt = LocalDateTime.of(2026, 5, 24, 15, 30);

        Order order = new Order(
                "AUC_001", "BUYER_123", "SELLER_456",
                new BigDecimal("5000"), new BigDecimal("500"),
                OrderStatus.PENDING, expectedResolvedAt,
                "Nguyen Van A", "0123456789", "Ha Noi"
        );

        assertNotNull(order.getId());
        assertNotNull(order.getCreatedAt());
        assertEquals("AUC_001", order.getAuctionId());
        assertEquals(expectedResolvedAt, order.getResolvedAt());
        assertEquals("Nguyen Van A", order.getConsigneeName());
    }

    @Test
    @DisplayName("Khởi tạo Order thành công khi dùng constructor không có resolvedAt")
    void testConstructor_WithoutResolvedAt_AssignsFields() {
        Order order = new Order(
                "AUC_001", "BUYER_123", "SELLER_456",
                new BigDecimal("5000"), new BigDecimal("500"),
                OrderStatus.PENDING,
                "Nguyen Van B", "0987654321", "TP.HCM"
        );

        assertEquals("AUC_001", order.getAuctionId());
        assertNull(order.getResolvedAt());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    @DisplayName("Xác nhận thành công đơn hàng đang ở trạng thái PENDING")
    void testConfirm_StatusIsPending_UpdatesStatusAndResolvedAt() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        order.confirm();

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertNotNull(order.getResolvedAt());
        assertTrue(order.getResolvedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Ném lỗi IllegalStateException khi xác nhận đơn hàng không phải PENDING")
    void testConfirm_StatusIsNotPending_ThrowsException() {
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELLED);

        IllegalStateException exception = assertThrows(IllegalStateException.class, order::confirm);

        assertEquals("Chỉ có thể xác nhận đơn hàng đang PENDING", exception.getMessage());
        assertEquals(OrderStatus.CANCELLED, order.getStatus()); // Đảm bảo trạng thái không bị thay đổi
    }

    @Test
    @DisplayName("Hủy thành công đơn hàng đang ở trạng thái PENDING")
    void testCancel_StatusIsPending_UpdatesStatusAndResolvedAt() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertNotNull(order.getResolvedAt());
        assertTrue(order.getResolvedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Ném lỗi IllegalStateException khi hủy đơn hàng không phải PENDING")
    void testCancel_StatusIsNotPending_ThrowsException() {
        Order order = new Order();
        order.setStatus(OrderStatus.CONFIRMED);

        IllegalStateException exception = assertThrows(IllegalStateException.class, order::cancel);

        assertEquals("Chỉ có thể hủy đơn hàng đang PENDING", exception.getMessage());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus()); // Đảm bảo trạng thái không bị thay đổi
    }
}