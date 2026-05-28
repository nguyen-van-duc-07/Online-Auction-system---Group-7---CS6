package service;

import com.auction.shared.enums.NotificationType;
import com.auction.shared.model.notification.Notification;
import com.auction.shared.response.NotificationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.NotificationRepository;
import servercontroller.Server;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notifRepo;

    private NotificationService notifService;
    private MockedStatic<Server> mockedServer;

    @BeforeEach
    void setUp() {
        notifService = new NotificationService(notifRepo);
        // Khởi tạo static mock cho Server
        mockedServer = mockStatic(Server.class);
    }

    @AfterEach
    void tearDown() {
        // Đóng static mock sau mỗi test case để tránh ảnh hưởng chéo
        if (mockedServer != null) {
            mockedServer.close();
        }
    }

    // ==========================================
    // TEST CASES FOR sendFromNotification
    // ==========================================

    @Test
    void sendFromNotification_saveSucceeds_sendsRealtimeNotification() {
        // Arrange
        Notification notification = new Notification("user123", NotificationType.SYSTEM, "Title", "Content", "ref123");
        notification.setId("notif123");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(notifRepo.save(notification)).thenReturn(true);

        // Act
        notifService.sendFromNotification(notification);

        // Assert
        verify(notifRepo).save(notification);
        mockedServer.verify(() -> Server.sendToUser(eq("user123"), any(NotificationDTO.class)));
    }

    @Test
    void sendFromNotification_saveFails_doesNotSendNotification() {
        // Arrange
        Notification notification = new Notification();
        notification.setId("notif123");
        notification.setUserId("user123");
        notification.setType(NotificationType.SYSTEM);

        when(notifRepo.save(notification)).thenReturn(false);

        // Act
        notifService.sendFromNotification(notification);

        // Assert
        verify(notifRepo).save(notification);
        mockedServer.verify(() -> Server.sendToUser(anyString(), any()), never());
    }

    // ==========================================
    // TEST CASES FOR sendNewAuctionNotification
    // ==========================================

    @Test
    void sendNewAuctionNotification_sendsBroadcast() {
        // Arrange
        String auctionId = "auction123";
        String itemName = "Bức tranh quý";
        BigDecimal startPrice = new BigDecimal("5000000.00");

        // Act
        notifService.sendNewAuctionNotification(auctionId, itemName, startPrice);

        // Assert
        mockedServer.verify(() -> Server.broadcastToBidders(argThat(dto -> {
            NotificationDTO notifDto = (NotificationDTO) dto;
            return NotificationType.SYSTEM == notifDto.getType() &&
                   notifDto.getTitle().contains("Phiên đấu giá mới") &&
                   notifDto.getContent().contains(itemName) &&
                   auctionId.equals(notifDto.getReferenceId());
        })));
    }

    // ==========================================
    // TEST CASES FOR OTHER METHODS
    // ==========================================

    @Test
    void getNotifications_callsRepo() {
        // Arrange
        String userId = "user123";
        List<Notification> expectedList = new ArrayList<>();
        expectedList.add(new Notification());

        when(notifRepo.findByUserId(userId)).thenReturn(expectedList);

        // Act
        List<Notification> actualList = notifService.getNotifications(userId);

        // Assert
        assertNotNull(actualList);
        assertEquals(expectedList, actualList);
        verify(notifRepo).findByUserId(userId);
    }

    @Test
    void getUnreadCount_callsRepo() {
        // Arrange
        String userId = "user123";
        when(notifRepo.countUnread(userId)).thenReturn(5);

        // Act
        int unreadCount = notifService.getUnreadCount(userId);

        // Assert
        assertEquals(5, unreadCount);
        verify(notifRepo).countUnread(userId);
    }

    @Test
    void markAsRead_callsRepo() {
        // Arrange
        String notifId = "notif123";
        when(notifRepo.markAsRead(notifId)).thenReturn(true);

        // Act
        boolean result = notifService.markAsRead(notifId);

        // Assert
        assertTrue(result);
        verify(notifRepo).markAsRead(notifId);
    }

    @Test
    void markAllAsRead_callsRepo() {
        // Arrange
        String userId = "user123";
        when(notifRepo.markAllAsRead(userId)).thenReturn(true);

        // Act
        boolean result = notifService.markAllAsRead(userId);

        // Assert
        assertTrue(result);
        verify(notifRepo).markAllAsRead(userId);
    }

    @Test
    void deleteExpired_callsRepo() {
        // Act
        notifService.deleteExpired();

        // Assert
        verify(notifRepo).deleteExpired();
    }
}
