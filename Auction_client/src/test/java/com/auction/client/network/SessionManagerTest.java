package com.auction.client.network;

import com.auction.shared.model.user.UserDTO;
import com.auction.shared.response.GetBalanceResponseDTO;
import javafx.beans.property.ObjectProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    @BeforeEach
    void setUp() {
        SessionManager.setCurrentUser(null);
        SessionManager.setCurrentAuctionId(null);
        SessionManager.setCurrentOrderId(null);
        SessionManager.balanceProperty().set(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Cập nhật và lấy User hiện tại")
    void testSetCurrentUser_ValidUser_ShouldStoreUser() {
        UserDTO user = new UserDTO();
        user.setId("USER_123");
        user.setAccountName("testuser");

        SessionManager.setCurrentUser(user);

        assertNotNull(SessionManager.getCurrentUser());
        assertEquals("USER_123", SessionManager.getCurrentUser().getId());
    }

    @Test
    @DisplayName("Cập nhật User thành null")
    void testSetCurrentUser_Null_ShouldStoreNull() {
        SessionManager.setCurrentUser(null);
        assertNull(SessionManager.getCurrentUser());
    }

    @Test
    @DisplayName("Cập nhật và lấy ID phiên đấu giá hiện tại")
    void testSetCurrentAuctionId_ValidId_ShouldStoreId() {
        SessionManager.setCurrentAuctionId("AUC_001");
        assertEquals("AUC_001", SessionManager.getCurrentAuctionId());
    }

    @Test
    @DisplayName("Cập nhật và lấy ID đơn hàng hiện tại")
    void testSetCurrentOrderId_ValidId_ShouldStoreId() {
        SessionManager.setCurrentOrderId("ORD_999");
        assertEquals("ORD_999", SessionManager.getCurrentOrderId());
    }

    @Test
    @DisplayName("updateBalance() với phản hồi thành công sẽ cập nhật số dư")
    void testUpdateBalance_SuccessResponse_ShouldUpdateBalance() {
        GetBalanceResponseDTO response = new GetBalanceResponseDTO();
        response.setSuccess(true);
        response.setBalance(new BigDecimal("500000.00"));

        SessionManager.updateBalance(response);

        assertEquals(0, new BigDecimal("500000.00").compareTo(SessionManager.getCurrentBalance()), "Số dư chưa được cập nhật đúng");
    }

    @Test
    @DisplayName("updateBalance() với phản hồi thất bại sẽ không cập nhật số dư")
    void testUpdateBalance_FailedResponse_ShouldNotUpdateBalance() {
        SessionManager.balanceProperty().set(new BigDecimal("1000.00"));

        GetBalanceResponseDTO response = new GetBalanceResponseDTO();
        response.setSuccess(false); // Thất bại
        response.setBalance(new BigDecimal("500000.00"));

        SessionManager.updateBalance(response);

        // Số dư phải giữ nguyên là 1000.00
        assertEquals(0, new BigDecimal("1000.00").compareTo(SessionManager.getCurrentBalance()), "Số dư không được phép thay đổi khi lỗi");
    }

    @Test
    @DisplayName("updateBalance() với null sẽ không gây lỗi và giữ nguyên số dư")
    void testUpdateBalance_NullResponse_ShouldNotUpdateBalance() {
        SessionManager.balanceProperty().set(new BigDecimal("2000.00"));

        assertDoesNotThrow(() -> {
            SessionManager.updateBalance(null);
        });

        assertEquals(0, new BigDecimal("2000.00").compareTo(SessionManager.getCurrentBalance()), "Số dư phải giữ nguyên khi response null");
    }

    @Test
    @DisplayName("Lấy ObjectProperty của số dư")
    void testBalanceProperty_ShouldReturnObjectProperty() {
        ObjectProperty<BigDecimal> property = SessionManager.balanceProperty();
        assertNotNull(property);
        assertEquals(0, BigDecimal.ZERO.compareTo(property.get()));
    }
}
