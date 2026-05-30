package scheduler;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.item.ItemDTO;
import com.auction.shared.model.order.Order;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.response.AuctionResultDTO;
import com.auction.shared.response.AuctionStatusUpdateDTO;
import config.ConnectionProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.AuctionRepository;
import servercontroller.Server;
import service.NotificationService;
import service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionStatusSchedulerTest {

    @Mock
    private AuctionRepository auctionRepo;

    @Mock
    private OrderService orderService;

    @Mock
    private NotificationService notifService;

    private AuctionStatusScheduler scheduler;
    private MockedStatic<Server> mockedServer;

    @BeforeEach
    void setUp() {
        scheduler = new AuctionStatusScheduler(
            auctionRepo,
            orderService,
            notifService
        );
        mockedServer = mockStatic(Server.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedServer != null) {
            mockedServer.close();
        }
    }
    // TEST CASES FOR updateAuctionStatus (Activation)

    @Test
    @DisplayName("Cập nhật trạng thái: Không làm gì khi không có phiên đấu giá cần chuyển trạng thái")
    void testUpdateAuctionStatus_NoAuctionsToActivateOrClose_DoesNothing() {
        when(auctionRepo.findAuctionsToActivate(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(auctionRepo.findAuctionsToCloseWithDetails(any(LocalDateTime.class))).thenReturn(Collections.emptyMap());
        scheduler.updateAuctionStatus();
        verify(auctionRepo, never()).activateAuctions(anyList());
        mockedServer.verify(() -> Server.broadcastToAuctionRoom(anyString(), any()), never());
        verify(notifService, never()).sendNewAuctionNotification(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Cập nhật trạng thái: Kích hoạt phiên đấu giá thành công")
    void testUpdateAuctionStatus_ActivatesAuctionsSuccessfully() {
        String auctionId = "auc123";
        List<String> activateIds = List.of(auctionId);
        ItemDTO item = new ItemDTO();
        item.setName("Bức tranh cổ");
        
        AuctionResponseDTO auctionResponse = new AuctionResponseDTO();
        auctionResponse.setId(auctionId);
        auctionResponse.setItem(item);
        auctionResponse.setStartPrice(new BigDecimal("1000000.00"));

        when(auctionRepo.findAuctionsToActivate(any(LocalDateTime.class))).thenReturn(activateIds);
        when(auctionRepo.findAuctionResponseDTOById(auctionId)).thenReturn(auctionResponse);
        when(auctionRepo.findAuctionsToCloseWithDetails(any(LocalDateTime.class))).thenReturn(Collections.emptyMap());
        scheduler.updateAuctionStatus();
        verify(auctionRepo).activateAuctions(activateIds);
        mockedServer.verify(() -> Server.broadcastToAuctionRoom(
            eq(auctionId),
            argThat(dto -> {
                AuctionStatusUpdateDTO updateDto = (AuctionStatusUpdateDTO) dto;
                return auctionId.equals(updateDto.getId()) &&
                    AuctionStatus.ACTIVE == updateDto.getAuctionStatus();
            })
        ));
        verify(notifService).sendNewAuctionNotification(auctionId, "Bức tranh cổ", new BigDecimal("1000000.00"));
    }
    // TEST CASES FOR updateAuctionStatus (Closing)

    @Test
    @DisplayName("Cập nhật trạng thái: Đóng phiên đấu giá có người chiến thắng thành công")
    void testUpdateAuctionStatus_ClosesAuctionWithWinnerSuccessfully() {
        String auctionId = "auc777";
        String sellerId = "seller123";
        String winnerId = "winner456";
        BigDecimal finalPrice = new BigDecimal("2500000.00");

        ItemDTO item = new ItemDTO();
        item.setName("Đồng hồ Thụy Sỹ");

        AuctionResponseDTO auctionToClose = new AuctionResponseDTO();
        auctionToClose.setId(auctionId);
        auctionToClose.setUserId(sellerId);
        auctionToClose.setHighestBidderId(winnerId);
        auctionToClose.setCurrentHighestPrice(finalPrice);
        auctionToClose.setItem(item);

        Map<String, AuctionResponseDTO> closeMap = new HashMap<>();
        closeMap.put(auctionId, auctionToClose);

        when(auctionRepo.findAuctionsToActivate(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(auctionRepo.findAuctionsToCloseWithDetails(any(LocalDateTime.class))).thenReturn(closeMap);
        when(auctionRepo.tryCloseExpiredAuction(eq(auctionId), any(LocalDateTime.class))).thenReturn(true);

        Order mockOrder = new Order();
        mockOrder.setId("order001");
        when(orderService.createOrder(auctionId, winnerId, finalPrice)).thenReturn(mockOrder);
        scheduler.updateAuctionStatus();
        mockedServer.verify(() -> Server.broadcastToAuctionRoom(
            eq(auctionId),
            argThat(dto -> {
                if (dto instanceof AuctionStatusUpdateDTO statusDto) {
                    return auctionId.equals(statusDto.getId()) &&
                        AuctionStatus.CLOSED == statusDto.getAuctionStatus();
                }
                return false;
            })
        ));
        mockedServer.verify(() -> Server.broadcastToAuctionRoom(
            eq(auctionId),
            argThat(dto -> {
                if (dto instanceof AuctionResultDTO resultDto) {
                    return auctionId.equals(resultDto.getAuctionId()) &&
                        winnerId.equals(resultDto.getWinnerId()) &&
                        "Đồng hồ Thụy Sỹ".equals(resultDto.getItemName()) &&
                        finalPrice.equals(resultDto.getFinalPrice());
                }
                return false;
            })
        ));
        verify(orderService).createOrder(auctionId, winnerId, finalPrice);
        // Verify win/end notification triggered
        verify(notifService, times(2)).sendFromNotification(any());
    }

    @Test
    @DisplayName("Cập nhật trạng thái: Đóng phiên đấu giá không có người chiến thắng thành công")
    void testUpdateAuctionStatus_ClosesAuctionWithNoWinnerSuccessfully() {
        String auctionId = "auc777";
        String sellerId = "seller123";
        BigDecimal startPrice = new BigDecimal("2500000.00");

        ItemDTO item = new ItemDTO();
        item.setName("Đồng hồ Thụy Sỹ");

        AuctionResponseDTO auctionToClose = new AuctionResponseDTO();
        auctionToClose.setId(auctionId);
        auctionToClose.setUserId(sellerId);
        auctionToClose.setHighestBidderId(null); // No highest bidder
        auctionToClose.setCurrentHighestPrice(startPrice);
        auctionToClose.setItem(item);

        Map<String, AuctionResponseDTO> closeMap = new HashMap<>();
        closeMap.put(auctionId, auctionToClose);

        when(auctionRepo.findAuctionsToActivate(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(auctionRepo.findAuctionsToCloseWithDetails(any(LocalDateTime.class))).thenReturn(closeMap);
        when(auctionRepo.tryCloseExpiredAuction(eq(auctionId), any(LocalDateTime.class))).thenReturn(true);
        scheduler.updateAuctionStatus();
        verify(auctionRepo).tryCloseExpiredAuction(eq(auctionId), any(LocalDateTime.class));
        mockedServer.verify(() -> Server.broadcastToAuctionRoom(
            eq(auctionId),
            argThat(dto -> {
                if (dto instanceof AuctionStatusUpdateDTO statusDto) {
                    return auctionId.equals(statusDto.getId()) &&
                        AuctionStatus.CLOSED == statusDto.getAuctionStatus();
                }
                return false;
            })
        ));
        // Should not broadcast win result or create order since winnerId is null
        mockedServer.verify(() ->
                Server.broadcastToAuctionRoom(
                    anyString(),
                    any(AuctionResultDTO.class)
                ),
            never()
        );        verify(orderService, never()).createOrder(anyString(), anyString(), any());
        verify(notifService, times(1)).sendFromNotification(any());
    }

    @Test
    @DisplayName("Cập nhật trạng thái: Bỏ qua xử lý khi không thể đóng phiên đấu giá hết hạn trong DB")
    void testUpdateAuctionStatus_TryCloseExpiredAuctionFails_SkipsClosingLogic() {
        String auctionId = "auc777";
        AuctionResponseDTO auctionToClose = new AuctionResponseDTO();
        auctionToClose.setId(auctionId);

        Map<String, AuctionResponseDTO> closeMap = new HashMap<>();
        closeMap.put(auctionId, auctionToClose);

        when(auctionRepo.findAuctionsToActivate(any(LocalDateTime.class))).thenReturn(Collections.emptyList());
        when(auctionRepo.findAuctionsToCloseWithDetails(any(LocalDateTime.class))).thenReturn(closeMap);
        when(auctionRepo.tryCloseExpiredAuction(eq(auctionId), any(LocalDateTime.class))).thenReturn(false);
        scheduler.updateAuctionStatus();
        verify(auctionRepo).tryCloseExpiredAuction(eq(auctionId), any(LocalDateTime.class));
        verify(auctionRepo, never()).findAuctionResponseDTOById(auctionId);
        mockedServer.verify(() -> Server.broadcastToAuctionRoom(anyString(), any()), never());
    }
}
