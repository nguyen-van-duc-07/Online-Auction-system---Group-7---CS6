package service;

import com.auction.shared.enums.NotificationType;
import com.auction.shared.model.notification.Notification;
import com.auction.shared.response.NotificationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    // TEST CASES FOR sendFromNotification

    @Test
    @DisplayName("Gửi thông báo thành công: Lưu DB và gửi realtime qua socket")
    void testSendFromNotification_SaveSucceeds_SendsRealtimeNotification() {
        Notification notification = new Notification("user123", NotificationType.SYSTEM, "Title", "Content", "ref123");
        notification.setId("notif123");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(notifRepo.save(notification)).thenReturn(true);
        notifService.sendFromNotification(notification);
        verify(notifRepo).save(notification);
        mockedServer.verify(() -> Server.sendToUser(eq("user123"), any(NotificationDTO.class)));
    }

    @Test
    @DisplayName("Gửi thông báo thất bại: Không gửi realtime khi lỗi lưu DB")
    void testSendFromNotification_SaveFails_DoesNotSendNotification() {
        Notification notification = new Notification();
        notification.setId("notif123");
        notification.setUserId("user123");
        notification.setType(NotificationType.SYSTEM);

        when(notifRepo.save(notification)).thenReturn(false);
        notifService.sendFromNotification(notification);
        verify(notifRepo).save(notification);
        mockedServer.verify(() -> Server.sendToUser(anyString(), any()), never());
    }
    // TEST CASES FOR sendNewAuctionNotification

    @Test
    @DisplayName("Gửi thông báo phiên đấu giá mới thành công (broadcast)")
    void testSendNewAuctionNotification_SendsBroadcast() {
        String auctionId = "auction123";
        String itemName = "Bức tranh quý";
        BigDecimal startPrice = new BigDecimal("5000000.00");
        notifService.sendNewAuctionNotification(auctionId, itemName, startPrice);
        mockedServer.verify(() -> Server.broadcastToAll(argThat(dto -> {
            NotificationDTO notifDto = (NotificationDTO) dto;
            return NotificationType.SYSTEM == notifDto.getType() &&
                   notifDto.getTitle().contains("Phiên đấu giá mới") &&
                   notifDto.getContent().contains(itemName) &&
                   auctionId.equals(notifDto.getReferenceId());
        })));
    }
    // TEST CASES FOR OTHER METHODS

    @Test
    @DisplayName("Lấy danh sách thông báo: Gọi đúng hàm từ repository")
    void testGetNotifications_CallsRepo() {
        String userId = "user123";
        List<Notification> expectedList = new ArrayList<>();
        expectedList.add(new Notification());

        when(notifRepo.findByUserId(userId)).thenReturn(expectedList);
        List<Notification> actualList = notifService.getNotifications(userId);
        assertNotNull(actualList);
        assertEquals(expectedList, actualList);
        verify(notifRepo).findByUserId(userId);
    }

    @Test
    @DisplayName("Đếm số thông báo chưa đọc: Gọi đúng hàm từ repository")
    void testGetUnreadCount_CallsRepo() {
        String userId = "user123";
        when(notifRepo.countUnread(userId)).thenReturn(5);
        int unreadCount = notifService.getUnreadCount(userId);
        assertEquals(5, unreadCount);
        verify(notifRepo).countUnread(userId);
    }

    @Test
    @DisplayName("Đánh dấu đã đọc: Gọi đúng hàm từ repository")
    void testMarkAsRead_CallsRepo() {
        String notifId = "notif123";
        when(notifRepo.markAsRead(notifId)).thenReturn(true);
        boolean result = notifService.markAsRead(notifId);
        assertTrue(result);
        verify(notifRepo).markAsRead(notifId);
    }

    @Test
    @DisplayName("Đánh dấu tất cả đã đọc: Gọi đúng hàm từ repository")
    void testMarkAllAsRead_CallsRepo() {
        String userId = "user123";
        when(notifRepo.markAllAsRead(userId)).thenReturn(true);
        boolean result = notifService.markAllAsRead(userId);
        assertTrue(result);
        verify(notifRepo).markAllAsRead(userId);
    }

    @Test
    @DisplayName("Xóa thông báo hết hạn: Gọi đúng hàm từ repository")
    void testDeleteExpired_CallsRepo() {
        notifService.deleteExpired();
        verify(notifRepo).deleteExpired();
    }
}
