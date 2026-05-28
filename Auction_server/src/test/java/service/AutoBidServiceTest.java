package service;

import com.auction.shared.model.auction.AutoBidConfig;
import com.auction.shared.request.CancelAutoBidRequestDTO;
import com.auction.shared.request.SetAutoBidRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import org.junit.jupiter.api.BeforeEach;
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

    // ==========================================
    // CÁC KỊCH BẢN KIỂM THỬ CHO setAutoBid
    // ==========================================

    @Test
    void setAutoBid_khongKichHoat_tatCauHinhAutoBid() {
        // Sắp đặt
        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO("user123", "auc123", new BigDecimal("5000000.00"), new BigDecimal("100000.00"), false);
        when(configRepo.deactivate("user123", "auc123")).thenReturn(true);

        // Thực thi
        boolean result = autoBidService.setAutoBid(req);

        // Xác minh
        assertTrue(result);
        verify(configRepo).deactivate("user123", "auc123");
        verifyNoInteractions(auctionRepo);
    }

    @Test
    void setAutoBid_khongTimThayPhienDauGia_traVeThatBai() {
        // Sắp đặt
        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO("user123", "auc123", new BigDecimal("5000000.00"), new BigDecimal("100000.00"), true);
        when(auctionRepo.findAuctionResponseDTOById("auc123")).thenReturn(null);

        // Thực thi
        boolean result = autoBidService.setAutoBid(req);

        // Xác minh
        assertFalse(result);
        verify(auctionRepo).findAuctionResponseDTOById("auc123");
        verify(configRepo, never()).save(any());
    }

    @Test
    void setAutoBid_buocGiaNhoHonYeuCau_traVeThatBai() {
        // Sắp đặt
        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO("user123", "auc123", new BigDecimal("5000000.00"), new BigDecimal("50000.00"), true); // Bước giá yêu cầu: 50.000 VNĐ
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId("auc123");
        auction.setMinStepPrice(new BigDecimal("100000.00")); // Bước giá tối thiểu của hệ thống: 100.000 VNĐ

        when(auctionRepo.findAuctionResponseDTOById("auc123")).thenReturn(auction);

        // Thực thi
        boolean result = autoBidService.setAutoBid(req);

        // Xác minh
        assertFalse(result);
        verify(auctionRepo).findAuctionResponseDTOById("auc123");
        verify(configRepo, never()).save(any());
    }

    @Test
    void setAutoBid_giaToiDaNhoHonHoacBangGiaHienTai_traVeThatBai() {
        // Sắp đặt
        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO("user123", "auc123", new BigDecimal("3000000.00"), new BigDecimal("100000.00"), true); // Giá tối đa: 3.000.000 VNĐ
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId("auc123");
        auction.setMinStepPrice(new BigDecimal("100000.00"));
        auction.setCurrentHighestPrice(new BigDecimal("3000000.00")); // Giá hiện tại: 3.000.000 VNĐ (Bằng giá tối đa)

        when(auctionRepo.findAuctionResponseDTOById("auc123")).thenReturn(auction);

        // Thực thi
        boolean result = autoBidService.setAutoBid(req);

        // Xác minh
        assertFalse(result);
        verify(auctionRepo).findAuctionResponseDTOById("auc123");
        verify(configRepo, never()).save(any());
    }

    @Test
    void setAutoBid_hopLe_luuThanhCong() {
        // Sắp đặt
        SetAutoBidRequestDTO req = new SetAutoBidRequestDTO("user123", "auc123", new BigDecimal("5000000.00"), new BigDecimal("100000.00"), true);
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId("auc123");
        auction.setMinStepPrice(new BigDecimal("100000.00"));
        auction.setCurrentHighestPrice(new BigDecimal("3000000.00"));

        when(auctionRepo.findAuctionResponseDTOById("auc123")).thenReturn(auction);
        when(configRepo.save(any(AutoBidConfig.class))).thenReturn(true);

        // Thực thi
        boolean result = autoBidService.setAutoBid(req);

        // Xác minh
        assertTrue(result);
        verify(auctionRepo).findAuctionResponseDTOById("auc123");
        verify(configRepo).save(argThat(config -> 
            "user123".equals(config.getUserId()) &&
            "auc123".equals(config.getAuctionId()) &&
            new BigDecimal("5000000.00").equals(config.getMaxPrice()) &&
            new BigDecimal("100000.00").equals(config.getStepAmount())
        ));
    }

    // ==========================================
    // CÁC KỊCH BẢN KIỂM THỬ CHO cancelAutoBid
    // ==========================================

    @Test
    void cancelAutoBid_huyThanhCong() {
        // Sắp đặt
        CancelAutoBidRequestDTO req = new CancelAutoBidRequestDTO("user123", "auc123");
        when(configRepo.deactivate("user123", "auc123")).thenReturn(true);

        // Thực thi
        boolean result = autoBidService.cancelAutoBid(req);

        // Xác minh
        assertTrue(result);
        verify(configRepo).deactivate("user123", "auc123");
    }
}
