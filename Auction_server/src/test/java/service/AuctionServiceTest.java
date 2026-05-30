package service;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.enums.ItemType;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.item.ItemDTO;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.request.UpdateAuctionStatusRequestDTO;
import com.auction.shared.request.UploadItemRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.response.UpdateAuctionStatusResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.AuctionRepository;
import repository.BidTransactionRepository;
import servercontroller.Server;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepo;

    @Mock
    private BidTransactionRepository bidRepo;

    private AuctionService auctionService;
    private MockedStatic<Server> mockedServer;

    @BeforeEach
    void setUp() {
        auctionService = new AuctionService(auctionRepo, bidRepo);
        mockedServer = mockStatic(Server.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedServer != null) {
            mockedServer.close();
        }
    }

    @Test
    @DisplayName("Tải lên phiên đấu giá mới thành công và trả về true")
    void testUploadNewAuction_ValidRequest_ReturnsTrue() {
        UploadItemRequestDTO req = new UploadItemRequestDTO(
            "user123",
            "Bàn đồ cổ",
            ItemType.OTHER,
            "Mô tả chi tiết",
            new BigDecimal("2000000.00"),
            new BigDecimal("100000.00"),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2),
            null,
            null,
            null
        );

        when(auctionRepo.saveAuction(any(Auction.class), any())).thenReturn(true);

        boolean result = auctionService.uploadNewAuction(req);

        assertTrue(result);
        verify(auctionRepo).saveAuction(any(Auction.class), any());
    }

    @Test
    @DisplayName("Tải lên phiên đấu giá thất bại do lỗi lưu DB và trả về false")
    void testUploadNewAuction_SaveFails_ReturnsFalse() {
        UploadItemRequestDTO req = new UploadItemRequestDTO(
            "user123",
            "Bàn đồ cổ",
            ItemType.OTHER,
            "Mô tả chi tiết",
            new BigDecimal("2000000.00"),
            new BigDecimal("100000.00"),
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2),
            null,
            null,
            null
        );

        when(auctionRepo.saveAuction(any(Auction.class), any())).thenReturn(false);

        boolean result = auctionService.uploadNewAuction(req);

        assertFalse(result);
        verify(auctionRepo).saveAuction(any(Auction.class), any());
    }

    @Test
    @DisplayName("Tìm kiếm phiên đấu giá theo ID thành công")
    void testFindAuctionById_ValidId_DelegatesToRepository() {
        String id = "auc123";
        AuctionResponseDTO expected = new AuctionResponseDTO();
        expected.setId(id);
        when(auctionRepo.findAuctionById(id)).thenReturn(expected);

        AuctionResponseDTO actual = auctionService.findAuctionById(id);

        assertEquals(expected, actual);
        verify(auctionRepo).findAuctionById(id);
    }

    @Test
    @DisplayName("Lấy danh sách các phiên đấu giá đang active cho client")
    void testGetActiveAuctionsForClient_ReturnsList() {
        List<AuctionDTO> expected = List.of(new AuctionDTO());
        when(auctionRepo.findAuctionsByStatusForBidder(AuctionStatus.ACTIVE)).thenReturn(expected);

        List<AuctionDTO> actual = auctionService.getActiveAuctionsForClient();

        assertEquals(expected, actual);
        verify(auctionRepo).findAuctionsByStatusForBidder(AuctionStatus.ACTIVE);
    }

    @Test
    @DisplayName("Lấy lịch sử giao dịch đấu giá thành công")
    void testGetAuctionHistory_ValidId_ReturnsBidHistory() {
        String auctionId = "auc123";
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId(auctionId);
        List<BidTransaction> history = List.of(new BidTransaction("auc123", "bidder123", new BigDecimal("1000.00")));

        when(auctionRepo.findAuctionResponseDTOById(auctionId)).thenReturn(auction);
        when(bidRepo.findRecentByAuctionId(auctionId, 20)).thenReturn(history);

        AuctionResponseDTO result = auctionService.getAuctionHistory(auctionId);

        assertNotNull(result);
        assertEquals(history, result.getBidHistory());
        verify(auctionRepo).findAuctionResponseDTOById(auctionId);
        verify(bidRepo).findRecentByAuctionId(auctionId, 20);
    }

    @Test
    @DisplayName("Cập nhật trạng thái bởi Admin thất bại khi phiên không tồn tại")
    void testUpdateAuctionStatusByAdmin_NonExistentAuction_ReturnsError() {
        UpdateAuctionStatusRequestDTO req = new UpdateAuctionStatusRequestDTO("auc999", AuctionStatus.ACTIVE);
        when(auctionRepo.findAuctionById("auc999")).thenReturn(null);

        UpdateAuctionStatusResponseDTO response = auctionService.updateAuctionStatusByAdmin(req);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Phiên đấu giá không tồn tại!", response.getMessage());
    }

    @Test
    @DisplayName("Admin đóng phiên đấu giá đang active thành công")
    void testUpdateAuctionStatusByAdmin_CloseActiveAuction_Success() {
        String auctionId = "auc123";
        UpdateAuctionStatusRequestDTO req = new UpdateAuctionStatusRequestDTO(auctionId, AuctionStatus.CLOSED);

        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId(auctionId);
        auction.setStatus(AuctionStatus.ACTIVE);

        when(auctionRepo.findAuctionById(auctionId)).thenReturn(auction);
        when(auctionRepo.updateAuctionEndTime(eq(auctionId), any(LocalDateTime.class))).thenReturn(true);

        UpdateAuctionStatusResponseDTO response = auctionService.updateAuctionStatusByAdmin(req);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Đóng phiên thành công! Phiên sẽ xử lý kết quả ngay lập tức.", response.getMessage());
        verify(auctionRepo).updateAuctionEndTime(eq(auctionId), any(LocalDateTime.class));
    }
}
