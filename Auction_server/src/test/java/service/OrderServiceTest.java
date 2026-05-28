package service;

import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.item.ItemDTO;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.user.InfoDTO;
import com.auction.shared.model.user.ShopInfoDTO;
import com.auction.shared.response.AuctionResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.AuctionRepository;
import repository.OrderRepository;
import repository.SellerProfileRepository;
import repository.UserRepository;
import config.ConnectionProvider;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private AuctionRepository auctionRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private WalletService walletService;

    @Mock
    private SellerProfileRepository sellerProfileRepo;

    @Mock
    private NotificationService notifService;

    @Mock
    private ConnectionProvider connectionProvider;

    @Mock
    private Connection mockConnection;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
            orderRepo,
            auctionRepo,
            userRepo,
            walletService,
            sellerProfileRepo,
            notifService,
            connectionProvider
        );
    }

    // ==========================================
    // TEST CASES FOR createOrder
    // ==========================================

    @Test
    void createOrder_validRequest_savesOrderAndCommits() throws Exception {
        // Arrange
        String auctionId = "auc123";
        String buyerId = "buyer456";
        BigDecimal finalPrice = new BigDecimal("1000000.00");
        String sellerId = "seller789";

        ShopInfoDTO shopInfo = new ShopInfoDTO("SuperShop", "Hanoi");
        
        ItemDTO item = new ItemDTO();
        item.setName("Luxury Perfume");
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setItem(item);

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.getUserIdByAuctionId(auctionId)).thenReturn(sellerId);
        when(sellerProfileRepo.getShopInfo(sellerId)).thenReturn(shopInfo);
        when(userRepo.getAccountNameByUserId(buyerId)).thenReturn("John Doe");
        when(auctionRepo.findAuctionById(auctionId)).thenReturn(auction);

        // Act
        Order createdOrder = orderService.createOrder(auctionId, buyerId, finalPrice);

        // Assert
        assertNotNull(createdOrder);
        assertEquals(auctionId, createdOrder.getAuctionId());
        assertEquals(buyerId, createdOrder.getBuyerId());
        assertEquals(sellerId, createdOrder.getSellerId());
        assertEquals(finalPrice, createdOrder.getFinalPrice());
        assertEquals(new BigDecimal("100000.000"), createdOrder.getDepositAmount()); // 10% of finalPrice
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
        assertEquals("Luxury Perfume", createdOrder.getItemName());
        
        verify(mockConnection).setAutoCommit(false);
        verify(orderRepo).saveOrder(eq(mockConnection), any(Order.class));
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }

    @Test
    void createOrder_databaseException_rollsBackAndReturnsNull() throws Exception {
        // Arrange
        String auctionId = "auc123";
        String buyerId = "buyer456";
        BigDecimal finalPrice = new BigDecimal("1000000.00");

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.getUserIdByAuctionId(auctionId)).thenThrow(new RuntimeException("DB Connection Timeout"));

        // Act
        Order createdOrder = orderService.createOrder(auctionId, buyerId, finalPrice);

        // Assert
        assertNull(createdOrder);
        verify(mockConnection).rollback();
        verify(mockConnection).setAutoCommit(true);
        verify(orderRepo, never()).saveOrder(any(), any());
    }

    // ==========================================
    // TEST CASES FOR confirmOrder
    // ==========================================

    @Test
    void confirmOrder_validRequest_processesPaymentUpdatesOrderAndSendsNotifications() throws Exception {
        // Arrange
        String orderId = "ord999";
        InfoDTO buyerInfo = new InfoDTO("John Doe", "0987654321", "123 Street");

        Order order = new Order("auc123", "buyer456", "seller789", 
            new BigDecimal("1000000.00"), new BigDecimal("100000.00"), new BigDecimal("900000.00"),
            OrderStatus.PENDING, "John Doe", "SuperShop", "Hanoi", "Luxury Perfume");
        order.setId(orderId);

        ItemDTO item = new ItemDTO();
        item.setName("Luxury Perfume");
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setItem(item);

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(orderRepo.findById(orderId)).thenReturn(order);
        when(auctionRepo.findAuctionById("auc123")).thenReturn(auction);

        // Act
        boolean result = orderService.confirmOrder(orderId, buyerInfo);

        // Assert
        assertTrue(result);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals("0987654321", order.getPhoneNumber());
        assertEquals("123 Street", order.getAddress());

        verify(mockConnection).setAutoCommit(false);
        verify(walletService).processPayment(mockConnection, order);
        verify(orderRepo).updateOrder(mockConnection, order);
        verify(mockConnection).commit();
        verify(notifService, times(2)).sendFromNotification(any());
    }

    @Test
    void confirmOrder_orderNotFound_returnsFalse() throws Exception {
        // Arrange
        String orderId = "ord999";
        InfoDTO buyerInfo = new InfoDTO("John Doe", "0987654321", "123 Street");

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(orderRepo.findById(orderId)).thenReturn(null);

        // Act
        boolean result = orderService.confirmOrder(orderId, buyerInfo);

        // Assert
        assertFalse(result);
        verify(walletService, never()).processPayment(any(), any());
        verify(mockConnection, never()).commit();
    }

    @Test
    void confirmOrder_orderNotPending_throwsRuntimeException() throws Exception {
        // Arrange
        String orderId = "ord999";
        InfoDTO buyerInfo = new InfoDTO("John Doe", "0987654321", "123 Street");

        Order order = new Order("auc123", "buyer456", "seller789", 
            new BigDecimal("1000000.00"), new BigDecimal("100000.00"), new BigDecimal("900000.00"),
            OrderStatus.CONFIRMED, "John Doe", "SuperShop", "Hanoi", "Luxury Perfume");
        order.setId(orderId);

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(orderRepo.findById(orderId)).thenReturn(order);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.confirmOrder(orderId, buyerInfo);
        });

        assertTrue(exception.getMessage().contains("Đơn hàng đã được thanh toán hoặc bị hủy trước đó!"));
        verify(mockConnection).rollback();
    }

    // ==========================================
    // TEST CASES FOR cancelOrder
    // ==========================================

    @Test
    void cancelOrder_validRequest_cancelsOrderAndProcessesPenalty() throws Exception {
        // Arrange
        String orderId = "ord999";
        Order order = new Order("auc123", "buyer456", "seller789", 
            new BigDecimal("1000000.00"), new BigDecimal("100000.00"), new BigDecimal("900000.00"),
            OrderStatus.PENDING, "John Doe", "SuperShop", "Hanoi", "Luxury Perfume");
        order.setId(orderId);

        ItemDTO item = new ItemDTO();
        item.setName("Luxury Perfume");
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setItem(item);

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(orderRepo.findById(orderId)).thenReturn(order);
        when(auctionRepo.findAuctionById("auc123")).thenReturn(auction);

        // Act
        boolean result = orderService.cancelOrder(orderId);

        // Assert
        assertTrue(result);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepo).updateOrder(mockConnection, order);
        verify(walletService).processCancelPenalty(mockConnection, order);
        verify(mockConnection).commit();
        verify(notifService, times(2)).sendFromNotification(any());
    }

    // ==========================================
    // TEST CASES FOR cancelExpiredOrders
    // ==========================================

    @Test
    void cancelExpiredOrders_findsExpiredOrders_cancelsThemAndSendsNotifications() throws Exception {
        // Arrange
        Order order = new Order("auc123", "buyer456", "seller789", 
            new BigDecimal("1000000.00"), new BigDecimal("100000.00"), new BigDecimal("900000.00"),
            OrderStatus.PENDING, "John Doe", "SuperShop", "Hanoi", "Luxury Perfume");
        order.setId("ordExpired1");

        List<Order> expiredOrders = new ArrayList<>();
        expiredOrders.add(order);

        ItemDTO item = new ItemDTO();
        item.setName("Luxury Perfume");
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setItem(item);

        when(orderRepo.findExpiredPendingOrders(any(LocalDateTime.class))).thenReturn(expiredOrders);
        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.findAuctionById("auc123")).thenReturn(auction);

        // Act
        orderService.cancelExpiredOrders();

        // Assert
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepo).updateOrder(mockConnection, order);
        verify(walletService).processCancelPenalty(mockConnection, order);
        verify(mockConnection).commit();
        verify(notifService, times(2)).sendFromNotification(any());
    }
}
