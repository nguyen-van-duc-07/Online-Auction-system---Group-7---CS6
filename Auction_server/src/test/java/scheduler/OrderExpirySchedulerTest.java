package scheduler;

import org.junit.jupiter.api.BeforeEach;
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
    void checkExpiredOrders_callsOrderServiceCancelExpiredOrders() {
        // Act
        expiryScheduler.checkExpiredOrders();

        // Assert
        verify(orderService).cancelExpiredOrders();
    }

    @Test
    void checkExpiredOrders_exceptionThrown_doesNotPropagateException() {
        // Arrange
        doThrow(new RuntimeException("Database timeout")).when(orderService).cancelExpiredOrders();

        // Act & Assert
        // Should not throw exception because it is caught internally
        expiryScheduler.checkExpiredOrders();

        verify(orderService).cancelExpiredOrders();
    }
}
