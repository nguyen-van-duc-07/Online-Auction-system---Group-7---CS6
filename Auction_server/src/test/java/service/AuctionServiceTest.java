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

    // ==========================================
    // CÁC KỊCH BẢN KIỂM THỬ CHO uploadNewAuction
    // ==========================================

    @Test
    void uploadNewAuction_luuThanhCong_traVeTrue() {
        // Sắp đặt
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

        // Thực thi
        boolean result = auctionService.uploadNewAuction(req);

        // Xác minh
        assertTrue(result);
        verify(auctionRepo).saveAuction(any(Auction.class), any());
    }

    @Test
    void uploadNewAuction_luuThatBai_traVeFalse() {
        // Sắp đặt
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

        // Thực thi
        boolean result = auctionService.uploadNewAuction(req);

        // Xác minh
        assertFalse(result);
        verify(auctionRepo).saveAuction(any(Auction.class), any());
    }

    // ==========================================
    // CÁC KỊCH BẢN KIỂM THỬ CHO CÁC PHƯƠNG THỨC TRUY VẤN
    // ==========================================

    @Test
    void findAuctionById_goiRepository() {
        // Sắp đặt
        String id = "auc123";
        AuctionResponseDTO expected = new AuctionResponseDTO();
        expected.setId(id);
        when(auctionRepo.findAuctionById(id)).thenReturn(expected);

        // Thực thi
        AuctionResponseDTO actual = auctionService.findAuctionById(id);

        // Xác minh
        assertEquals(expected, actual);
        verify(auctionRepo).findAuctionById(id);
    }

    @Test
    void getActiveAuctionsForClient_traVeDanhSach() {
        // Sắp đặt
        List<AuctionDTO> expected = List.of(new AuctionDTO());
        when(auctionRepo.findActiveAuctions()).thenReturn(expected);

        // Thực thi
        List<AuctionDTO> actual = auctionService.getActiveAuctionsForClient();

        // Xác minh
        assertEquals(expected, actual);
        verify(auctionRepo).findActiveAuctions();
    }

    @Test
    void getAuctionHistory_layLichSuGiaoDich() {
        // Sắp đặt
        String auctionId = "auc123";
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId(auctionId);
        List<BidTransaction> history = List.of(new BidTransaction("auc123", "bidder123", new BigDecimal("1000.00")));

        when(auctionRepo.findAuctionResponseDTOById(auctionId)).thenReturn(auction);
        when(bidRepo.findRecentByAuctionId(auctionId, 20)).thenReturn(history);

        // Thực thi
        AuctionResponseDTO result = auctionService.getAuctionHistory(auctionId);

        // Xác minh
        assertNotNull(result);
        assertEquals(history, result.getBidHistory());
        verify(auctionRepo).findAuctionResponseDTOById(auctionId);
        verify(bidRepo).findRecentByAuctionId(auctionId, 20);
    }

    // ==========================================
    // KIỂM THỬ CẬP NHẬT TRẠNG THÁI CỦA ADMIN
    // ==========================================

    @Test
    void updateAuctionStatusByAdmin_phienKhongTonTai_traVeLoi() {
        // Sắp đặt
        UpdateAuctionStatusRequestDTO req = new UpdateAuctionStatusRequestDTO("auc999", AuctionStatus.ACTIVE);
        when(auctionRepo.findAuctionById("auc999")).thenReturn(null);

        // Thực thi
        UpdateAuctionStatusResponseDTO response = auctionService.updateAuctionStatusByAdmin(req);

        // Xác minh
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Phiên đấu giá không tồn tại!", response.getMessage());
    }

    @Test
    void updateAuctionStatusByAdmin_dongPhienDangActive_thanhCong() {
        // Sắp đặt
        String auctionId = "auc123";
        UpdateAuctionStatusRequestDTO req = new UpdateAuctionStatusRequestDTO(auctionId, AuctionStatus.CLOSED);

        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId(auctionId);
        auction.setStatus(AuctionStatus.ACTIVE);

        when(auctionRepo.findAuctionById(auctionId)).thenReturn(auction);
        when(auctionRepo.updateAuctionEndTime(eq(auctionId), any(LocalDateTime.class))).thenReturn(true);

        // Thực thi
        UpdateAuctionStatusResponseDTO response = auctionService.updateAuctionStatusByAdmin(req);

        // Xác minh
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Đóng phiên thành công! Phiên sẽ xử lý kết quả ngay lập tức.", response.getMessage());
        verify(auctionRepo).updateAuctionEndTime(eq(auctionId), any(LocalDateTime.class));
    }
}
