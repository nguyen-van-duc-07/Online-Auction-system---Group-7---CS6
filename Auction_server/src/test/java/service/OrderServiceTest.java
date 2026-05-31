package service;

import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.item.ItemDTO;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.user.InfoDTO;
import com.auction.shared.model.user.ShopInfoDTO;
import com.auction.shared.response.AuctionResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    // TEST CASES FOR createOrder

    @Test
    @DisplayName("Tạo đơn hàng hợp lệ: Lưu DB và commit thành công")
    void testCreateOrder_ValidRequest_SavesOrderAndCommits() throws Exception {
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
        Order createdOrder = orderService.createOrder(auctionId, buyerId, finalPrice);
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
    @DisplayName("Tạo đơn hàng lỗi DB: Rollback và trả về null")
    void testCreateOrder_DatabaseException_RollsBackAndReturnsNull() throws Exception {
        String auctionId = "auc123";
        String buyerId = "buyer456";
        BigDecimal finalPrice = new BigDecimal("1000000.00");

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.getUserIdByAuctionId(auctionId)).thenThrow(new RuntimeException("DB Connection Timeout"));
        Order createdOrder = orderService.createOrder(auctionId, buyerId, finalPrice);
        assertNull(createdOrder);
        verify(mockConnection).rollback();
        verify(mockConnection).setAutoCommit(true);
        verify(orderRepo, never()).saveOrder(any(), any());
    }
    // TEST CASES FOR confirmOrder

    @Test
    @DisplayName("Xác nhận đơn hàng: Thanh toán thành công, cập nhật đơn hàng và gửi thông báo")
    void testConfirmOrder_ValidRequest_ProcessesPaymentUpdatesOrderAndSendsNotifications() throws Exception {
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
        boolean result = orderService.confirmOrder(orderId, buyerInfo);
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
    @DisplayName("Xác nhận đơn hàng thất bại: Không tìm thấy đơn hàng")
    void testConfirmOrder_OrderNotFound_ReturnsFalse() throws Exception {
        String orderId = "ord999";
        InfoDTO buyerInfo = new InfoDTO("John Doe", "0987654321", "123 Street");

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(orderRepo.findById(orderId)).thenReturn(null);
        boolean result = orderService.confirmOrder(orderId, buyerInfo);
        assertFalse(result);
        verify(walletService, never()).processPayment(any(), any());
        verify(mockConnection, never()).commit();
    }

    @Test
    @DisplayName("Xác nhận đơn hàng thất bại: Đơn hàng không ở trạng thái Pending")
    void testConfirmOrder_OrderNotPending_ThrowsRuntimeException() throws Exception {
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
    // TEST CASES FOR cancelOrder

    @Test
    @DisplayName("Hủy đơn hàng: Cập nhật trạng thái và xử lý trừ tiền phạt")
    void testCancelOrder_ValidRequest_CancelsOrderAndProcessesPenalty() throws Exception {
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
        boolean result = orderService.cancelOrder(orderId);
        assertTrue(result);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepo).updateOrder(mockConnection, order);
        verify(walletService).processCancelPenalty(mockConnection, order);
        verify(mockConnection).commit();
        verify(notifService, times(2)).sendFromNotification(any());
    }
    // TEST CASES FOR cancelExpiredOrders

    @Test
    @DisplayName("Hủy đơn hàng quá hạn: Tự động hủy và gửi thông báo")
    void testCancelExpiredOrders_FindsExpiredOrders_CancelsThemAndSendsNotifications() throws Exception {
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
        orderService.cancelExpiredOrders();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepo).updateOrder(mockConnection, order);
        verify(walletService).processCancelPenalty(mockConnection, order);
        verify(mockConnection).commit();
        verify(notifService, times(2)).sendFromNotification(any());
    }
}
