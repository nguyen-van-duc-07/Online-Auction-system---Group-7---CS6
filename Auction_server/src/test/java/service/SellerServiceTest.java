package service;

import com.auction.shared.enums.SellerRegisterStatus;
import com.auction.shared.model.user.SellerProfile;
import com.auction.shared.request.CheckingSellerProfileRequestDTO;
import com.auction.shared.request.SellerRegisterRequestDTO;
import com.auction.shared.request.UpdateSellerProfileStatusRequestDTO;
import com.auction.shared.response.UpdateSellerProfileStatusResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.SellerProfileRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerServiceTest {

    @Mock
    private SellerProfileRepository sellerRepo;

    @Mock
    private AuctionService auctionService;

    @InjectMocks
    private SellerService sellerService;
    // CÁC KIỂM THỬ CHO PHƯƠNG THỨC sellerRegister

    private SellerRegisterRequestDTO createRegisterRequest(
        String userId, String brandName, String citizenIdentityCard, String location, String bankAccount, String bankName
    ) {
        SellerRegisterRequestDTO req = new SellerRegisterRequestDTO();
        req.setUserId(userId);
        req.setBrandName(brandName);
        req.setCitizenIdentityCard(citizenIdentityCard);
        req.setLocation(location);
        req.setBankAccount(bankAccount);
        req.setBankName(bankName);
        return req;
    }

    @Test
    @DisplayName("Đăng ký người bán thành công: Trả về true và gửi thông báo")
    void testSellerRegister_Success_ReturnsTrueAndSendsNotification() {
        SellerRegisterRequestDTO req = createRegisterRequest(
            "user123", "BrandX", "123456789", "Ha Noi", "000111222", "VietinBank"
        );
        when(sellerRepo.createSellerProfile(any(SellerProfile.class))).thenReturn(true);

        // Thực thi & Xác minh
        NotificationService mockNotif = mock(NotificationService.class);
        try (MockedStatic<NotificationService> staticMock = mockStatic(NotificationService.class)) {
            staticMock.when(NotificationService::getInstance).thenReturn(mockNotif);

            boolean result = sellerService.sellerRegister(req);

            assertTrue(result);
            verify(sellerRepo).createSellerProfile(any(SellerProfile.class));
            verify(mockNotif).sendFromNotification(any());
        }
    }

    @Test
    @DisplayName("Đăng ký người bán thất bại: Trả về false và không gửi thông báo")
    void testSellerRegister_Failure_ReturnsFalseAndNoNotification() {
        SellerRegisterRequestDTO req = createRegisterRequest(
            "user123", "BrandX", "123456789", "Ha Noi", "000111222", "VietinBank"
        );
        when(sellerRepo.createSellerProfile(any(SellerProfile.class))).thenReturn(false);

        // Thực thi & Xác minh
        NotificationService mockNotif = mock(NotificationService.class);
        try (MockedStatic<NotificationService> staticMock = mockStatic(NotificationService.class)) {
            staticMock.when(NotificationService::getInstance).thenReturn(mockNotif);

            boolean result = sellerService.sellerRegister(req);

            assertFalse(result);
            verify(sellerRepo).createSellerProfile(any(SellerProfile.class));
            verify(mockNotif, never()).sendFromNotification(any());
        }
    }
    // CÁC KIỂM THỬ CHO PHƯƠNG THỨC isSellerProfileCreated

    @Test
    @DisplayName("Kiểm tra hồ sơ người bán: Đã tồn tại trả về true")
    void testIsSellerProfileCreated_ProfileExists_ReturnsTrue() {
        CheckingSellerProfileRequestDTO req = new CheckingSellerProfileRequestDTO("user123");
        when(sellerRepo.findProfileIdByUserId("user123")).thenReturn("profile123");

        boolean result = sellerService.isSellerProfileCreated(req);

        assertTrue(result);
        verify(sellerRepo).findProfileIdByUserId("user123");
    }

    @Test
    @DisplayName("Kiểm tra hồ sơ người bán: Không tồn tại trả về false")
    void testIsSellerProfileCreated_ProfileNotExists_ReturnsFalse() {
        CheckingSellerProfileRequestDTO req = new CheckingSellerProfileRequestDTO("user123");
        when(sellerRepo.findProfileIdByUserId("user123")).thenReturn(null);

        boolean result = sellerService.isSellerProfileCreated(req);

        assertFalse(result);
        verify(sellerRepo).findProfileIdByUserId("user123");
    }
    // CÁC KIỂM THỬ CHO PHƯƠNG THỨC sellerProfileStatus

    @Test
    @DisplayName("Lấy trạng thái hồ sơ người bán chính xác")
    void testSellerProfileStatus_ReturnsCorrectStatus() {
        when(sellerRepo.getSellerProfileStatus("user123")).thenReturn("REGISTERED");

        String status = sellerService.sellerProfileStatus("user123");

        assertEquals("REGISTERED", status);
        verify(sellerRepo).getSellerProfileStatus("user123");
    }
    // CÁC KIỂM THỬ CHO PHƯƠNG THỨC getSellerProfiles

    @Test
    @DisplayName("Lấy danh sách hồ sơ người bán hợp lệ")
    void testGetSellerProfiles_ReturnsValidList() {
        List<SellerProfile> dbProfiles = new ArrayList<>();
        SellerProfile p1 = new SellerProfile("user1", "Brand1", "CID1", "Loc1", "Acc1", "Bank1", "REGISTERED");
        SellerProfile p2 = new SellerProfile("user2", "Brand2", "CID2", "Loc2", "Acc2", "Bank2", "UNREGISTERED");
        dbProfiles.add(p1);
        dbProfiles.add(p2);

        when(sellerRepo.getAllSellerProfiles()).thenReturn(dbProfiles);

        List<SellerRegisterRequestDTO> result = sellerService.getSellerProfiles();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUserId());
        assertEquals("Brand1", result.get(0).getBrandName());
        assertEquals("user2", result.get(1).getUserId());
        assertEquals("Brand2", result.get(1).getBrandName());
        verify(sellerRepo).getAllSellerProfiles();
    }
    // CÁC KIỂM THỬ CHO handleUpdateSellerProfileStatus

    @Test
    @DisplayName("Cập nhật trạng thái hồ sơ: Xung đột trạng thái trả về lỗi")
    void testHandleUpdateSellerProfileStatus_StatusConflict_ReturnsError() {
        UpdateSellerProfileStatusRequestDTO req = new UpdateSellerProfileStatusRequestDTO(
            "user123", SellerRegisterStatus.REGISTERED, SellerRegisterStatus.UNREGISTERED
        );
        // Trạng thái hiện tại trong DB là DENIED, khác với UNREGISTERED mong đợi
        when(sellerRepo.getSellerProfileStatus("user123")).thenReturn("DENIED");

        UpdateSellerProfileStatusResponseDTO response = sellerService.handleUpdateSellerProfileStatus(req);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Xung đột dữ liệu"));
        verify(sellerRepo, never()).updateStatus(anyString(), any());
    }

    @Test
    @DisplayName("Cập nhật trạng thái hồ sơ: Không tìm thấy hồ sơ trả về lỗi")
    void testHandleUpdateSellerProfileStatus_ProfileNotFound_ReturnsError() {
        UpdateSellerProfileStatusRequestDTO req = new UpdateSellerProfileStatusRequestDTO(
            "user123", SellerRegisterStatus.REGISTERED, SellerRegisterStatus.UNREGISTERED
        );
        when(sellerRepo.getSellerProfileStatus("user123")).thenReturn(null);

        UpdateSellerProfileStatusResponseDTO response = sellerService.handleUpdateSellerProfileStatus(req);

        assertFalse(response.isSuccess());
        assertEquals("Hồ sơ người bán không tồn tại!", response.getMessage());
        verify(sellerRepo, never()).updateStatus(anyString(), any());
    }

    @Test
    @DisplayName("Duyệt hồ sơ thành công: Khôi phục phiên đấu giá và gửi thông báo")
    void testHandleUpdateSellerProfileStatus_ApproveSuccess_RestoresAuctionsAndSendsNotification() {
        UpdateSellerProfileStatusRequestDTO req = new UpdateSellerProfileStatusRequestDTO(
            "user123", SellerRegisterStatus.REGISTERED, SellerRegisterStatus.UNREGISTERED
        );
        when(sellerRepo.getSellerProfileStatus("user123")).thenReturn("UNREGISTERED");
        when(sellerRepo.updateStatus("user123", SellerRegisterStatus.REGISTERED)).thenReturn(true);

        NotificationService mockNotif = mock(NotificationService.class);
        try (MockedStatic<NotificationService> staticMock = mockStatic(NotificationService.class)) {
            staticMock.when(NotificationService::getInstance).thenReturn(mockNotif);

            UpdateSellerProfileStatusResponseDTO response = sellerService.handleUpdateSellerProfileStatus(req);

            assertTrue(response.isSuccess());
            assertEquals("Cập nhật trạng thái người bán thành công!", response.getMessage());
            
            // Xác minh kích hoạt khôi phục đấu giá
            verify(auctionService).restoreCanceledAuctionsBySellerUserId("user123");
            verify(auctionService, never()).cancelActiveAndWaitingAuctionsBySellerUserId(anyString());

            // Xác minh gửi thông báo
            verify(mockNotif).sendFromNotification(any());
        }
    }

    @Test
    @DisplayName("Từ chối hồ sơ thành công: Hủy phiên đấu giá và gửi thông báo")
    void testHandleUpdateSellerProfileStatus_RejectSuccess_CancelsAuctionsAndSendsNotification() {
        UpdateSellerProfileStatusRequestDTO req = new UpdateSellerProfileStatusRequestDTO(
            "user123", SellerRegisterStatus.DENIED, SellerRegisterStatus.UNREGISTERED
        );
        when(sellerRepo.getSellerProfileStatus("user123")).thenReturn("UNREGISTERED");
        when(sellerRepo.updateStatus("user123", SellerRegisterStatus.DENIED)).thenReturn(true);

        NotificationService mockNotif = mock(NotificationService.class);
        try (MockedStatic<NotificationService> staticMock = mockStatic(NotificationService.class)) {
            staticMock.when(NotificationService::getInstance).thenReturn(mockNotif);

            UpdateSellerProfileStatusResponseDTO response = sellerService.handleUpdateSellerProfileStatus(req);

            assertTrue(response.isSuccess());
            assertEquals("Cập nhật trạng thái người bán thành công!", response.getMessage());
            
            // Xác minh kích hoạt hủy đấu giá
            verify(auctionService).cancelActiveAndWaitingAuctionsBySellerUserId("user123");
            verify(auctionService, never()).restoreCanceledAuctionsBySellerUserId(anyString());

            // Xác minh gửi thông báo
            verify(mockNotif).sendFromNotification(any());
        }
    }

    @Test
    @DisplayName("Cập nhật trạng thái hồ sơ thất bại: Lỗi hệ thống trả về thông báo lỗi")
    void testHandleUpdateSellerProfileStatus_UpdateFailure_ReturnsError() {
        UpdateSellerProfileStatusRequestDTO req = new UpdateSellerProfileStatusRequestDTO(
            "user123", SellerRegisterStatus.REGISTERED, SellerRegisterStatus.UNREGISTERED
        );
        when(sellerRepo.getSellerProfileStatus("user123")).thenReturn("UNREGISTERED");
        when(sellerRepo.updateStatus("user123", SellerRegisterStatus.REGISTERED)).thenReturn(false);

        UpdateSellerProfileStatusResponseDTO response = sellerService.handleUpdateSellerProfileStatus(req);

        assertFalse(response.isSuccess());
        assertEquals("Lỗi cập nhật trạng thái vào cơ sở dữ liệu!", response.getMessage());
    }
}
