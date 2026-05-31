package scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.OrderService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderExpirySchedulerTest {

    @Mock
    private OrderService orderService;

    private OrderExpiryScheduler expiryScheduler;

    @BeforeEach
    void setUp() {
        expiryScheduler = new OrderExpiryScheduler(orderService);
    }

    @Test
    @DisplayName("Kiểm tra đơn hàng quá hạn: Gọi hàm hủy đơn hàng từ OrderService")
    void testCheckExpiredOrders_CallsOrderServiceCancelExpiredOrders() {
        expiryScheduler.checkExpiredOrders();
        verify(orderService).cancelExpiredOrders();
    }

    @Test
    @DisplayName("Kiểm tra đơn hàng quá hạn: Bắt lỗi và không ném ngoại lệ ra ngoài")
    void testCheckExpiredOrders_ExceptionThrown_DoesNotPropagateException() {
        doThrow(new RuntimeException("Database timeout")).when(orderService).cancelExpiredOrders();

        // Act & Assert
        // Should not throw exception because it is caught internally
        expiryScheduler.checkExpiredOrders();

        verify(orderService).cancelExpiredOrders();
    }
}
