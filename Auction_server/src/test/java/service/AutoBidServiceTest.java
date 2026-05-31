package service;

import com.auction.shared.model.auction.AutoBidConfig;
import com.auction.shared.request.CancelAutoBidRequestDTO;
import com.auction.shared.request.SetAutoBidRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.AuctionRepository;
import repository.AutoBidConfigRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AutoBidServiceTest {

    @Mock
    private AutoBidConfigRepository configRepo;

    @Mock
    private AuctionRepository auctionRepo;

    private AutoBidService autoBidService;

    @BeforeEach
    void setUp() {
        autoBidService = new AutoBidService(configRepo, auctionRepo);
    }
    // CÁC KỊCH BẢN KIỂM THỬ CHO setAutoBid

    @Test
    @DisplayName("Tắt cấu hình AutoBid khi không kích hoạt")
    void testSetAutoBid_Deactivate_TurnsOffAutoBidConfig() {
        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO("user123", "auc123", new BigDecimal("5000000.00"), new BigDecimal("100000.00"), false);
        when(configRepo.deactivate("user123", "auc123")).thenReturn(true);
        boolean result = autoBidService.setAutoBid(req);
        assertTrue(result);
        verify(configRepo).deactivate("user123", "auc123");
        verifyNoInteractions(auctionRepo);
    }

    @Test
    @DisplayName("Cài đặt AutoBid thất bại do không tìm thấy phiên đấu giá")
    void testSetAutoBid_AuctionNotFound_ReturnsFailure() {
        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO("user123", "auc123", new BigDecimal("5000000.00"), new BigDecimal("100000.00"), true);
        when(auctionRepo.findAuctionResponseDTOById("auc123")).thenReturn(null);
        boolean result = autoBidService.setAutoBid(req);
        assertFalse(result);
        verify(auctionRepo).findAuctionResponseDTOById("auc123");
        verify(configRepo, never()).save(any());
    }

    @Test
    @DisplayName("Cài đặt AutoBid thất bại do bước giá nhỏ hơn yêu cầu")
    void testSetAutoBid_StepPriceTooLow_ReturnsFailure() {
        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO("user123", "auc123", new BigDecimal("5000000.00"), new BigDecimal("50000.00"), true); // Bước giá yêu cầu: 50.000 VNĐ
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId("auc123");
        auction.setMinStepPrice(new BigDecimal("100000.00")); // Bước giá tối thiểu của hệ thống: 100.000 VNĐ

        when(auctionRepo.findAuctionResponseDTOById("auc123")).thenReturn(auction);
        boolean result = autoBidService.setAutoBid(req);
        assertFalse(result);
        verify(auctionRepo).findAuctionResponseDTOById("auc123");
        verify(configRepo, never()).save(any());
    }

    @Test
    @DisplayName("Cài đặt AutoBid thất bại do giá tối đa nhỏ hơn hoặc bằng giá hiện tại")
    void testSetAutoBid_MaxPriceTooLow_ReturnsFailure() {
        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO("user123", "auc123", new BigDecimal("3000000.00"), new BigDecimal("100000.00"), true); // Giá tối đa: 3.000.000 VNĐ
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId("auc123");
        auction.setMinStepPrice(new BigDecimal("100000.00"));
        auction.setCurrentHighestPrice(new BigDecimal("3000000.00")); // Giá hiện tại: 3.000.000 VNĐ (Bằng giá tối đa)

        when(auctionRepo.findAuctionResponseDTOById("auc123")).thenReturn(auction);
        boolean result = autoBidService.setAutoBid(req);
        assertFalse(result);
        verify(auctionRepo).findAuctionResponseDTOById("auc123");
        verify(configRepo, never()).save(any());
    }

    @Test
    @DisplayName("Cài đặt AutoBid thành công với thông tin hợp lệ")
    void testSetAutoBid_ValidRequest_SavesSuccessfully() {
        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO("user123", "auc123", new BigDecimal("5000000.00"), new BigDecimal("100000.00"), true);
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId("auc123");
        auction.setMinStepPrice(new BigDecimal("100000.00"));
        auction.setCurrentHighestPrice(new BigDecimal("3000000.00"));

        when(auctionRepo.findAuctionResponseDTOById("auc123")).thenReturn(auction);
        when(configRepo.save(any(AutoBidConfig.class))).thenReturn(true);
        boolean result = autoBidService.setAutoBid(req);
        assertTrue(result);
        verify(auctionRepo).findAuctionResponseDTOById("auc123");
        verify(configRepo).save(argThat(config -> 
            "user123".equals(config.getUserId()) &&
            "auc123".equals(config.getAuctionId()) &&
            new BigDecimal("5000000.00").equals(config.getMaxPrice()) &&
            new BigDecimal("100000.00").equals(config.getStepAmount())
        ));
    }
    // CÁC KỊCH BẢN KIỂM THỬ CHO cancelAutoBid

    @Test
    @DisplayName("Hủy AutoBid thành công")
    void testCancelAutoBid_Success() {
        CancelAutoBidRequestDTO req = new CancelAutoBidRequestDTO("user123", "auc123");
        when(configRepo.deactivate("user123", "auc123")).thenReturn(true);
        boolean result = autoBidService.cancelAutoBid(req);
        assertTrue(result);
        verify(configRepo).deactivate("user123", "auc123");
    }
}
