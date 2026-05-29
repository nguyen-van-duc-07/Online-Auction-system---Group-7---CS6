package servercontroller;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.enums.SellerRegisterStatus;
import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.User;
import com.auction.shared.request.*;
import com.auction.shared.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.AutoBidConfigRepository;
import repository.SellerProfileRepository;
import service.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestDispatcherTest {

    @Mock
    private AuthService authService;

    @Mock
    private AuctionService auctionService;

    @Mock
    private BidService bidService;

    @Mock
    private SellerService sellerService;

    @Mock
    private OrderService orderService;

    @Mock
    private AutoBidService autoBidService;

    @Mock
    private WalletService walletService;

    @Mock
    private NotificationService notifService;

    @Mock
    private SellerProfileRepository sellerProfileRepo;

    @Mock
    private AutoBidConfigRepository autoBidRepo;

    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new RequestDispatcher(
            authService,
            auctionService,
            bidService,
            sellerService,
            orderService,
            autoBidService,
            walletService,
            notifService,
            sellerProfileRepo,
            autoBidRepo
        );
    }
    // CÁC KỊCH BẢN KIỂM THỬ CHO ĐĂNG NHẬP

    @Test
    @DisplayName("Đăng nhập thành công với dữ liệu hợp lệ")
    void testLogin_ValidRequest_ReturnsSuccess() {
        LoginRequestDTO req = new LoginRequestDTO("0987654321", "pass123");
        Bidder bidder = new Bidder();
        bidder.setId("user123");
        bidder.setRole(UserRole.BIDDER);
        bidder.setAccountName("Nguyen Van A");

        when(authService.login(req)).thenReturn(bidder);
        LoginResponseDTO response = dispatcher.login(req);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Đăng nhập thành công!", response.getMessage());
        assertEquals("user123", response.getUser().getId());
        verify(authService).login(req);
    }

    @Test
    @DisplayName("Đăng nhập thất bại do sai thông tin")
    void testLogin_InvalidCredentials_ReturnsFailure() {
        LoginRequestDTO req = new LoginRequestDTO("0987654321", "wrongPass");
        when(authService.login(req)).thenReturn(null);
        LoginResponseDTO response = dispatcher.login(req);
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Sai tài khoản hoặc mật khẩu", response.getMessage());
        verify(authService).login(req);
    }
    // CÁC KỊCH BẢN KIỂM THỬ CHO ĐẶT GIÁ

    @Test
    @DisplayName("Gọi hàm đặt giá từ BidService thành công")
    void testPlaceBid_ValidRequest_DelegatesToBidService() {
        PlaceBidRequestDTO req = new PlaceBidRequestDTO("auc123", "bidder123", "Bidder Name", new BigDecimal("1500000.00"));
        PlaceBidResponseDTO expectedResponse = new PlaceBidResponseDTO(true, "Đặt giá thành công");
        when(bidService.placeBid(req)).thenReturn(expectedResponse);
        PlaceBidResponseDTO actualResponse = dispatcher.placeBid(req);
        assertEquals(expectedResponse, actualResponse);
        verify(bidService).placeBid(req);
    }
    // CÁC KỊCH BẢN KIỂM THỬ CHO SỐ DƯ VÍ

    @Test
    @DisplayName("Lấy số dư thành công từ WalletService")
    void testGetBalance_ValidRequest_DelegatesToWalletService() throws Exception {
        String userId = "user123";
        BigDecimal expectedBalance = new BigDecimal("5000000.00");
        when(walletService.getBalance(userId)).thenReturn(expectedBalance);
        GetBalanceResponseDTO response = dispatcher.getBalance(userId);
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(expectedBalance, response.getBalance());
        verify(walletService).getBalance(userId);
    }

    @Test
    @DisplayName("Lấy số dư thất bại trả về 0 khi có ngoại lệ")
    void testGetBalance_ExceptionThrown_ReturnsZero() throws Exception {
        String userId = "user123";
        when(walletService.getBalance(userId)).thenThrow(new RuntimeException("Lỗi kết nối DB"));
        GetBalanceResponseDTO response = dispatcher.getBalance(userId);
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        verify(walletService).getBalance(userId);
    }
    // CÁC KỊCH BẢN KIỂM THỬ CHO ADMIN CẬP NHẬT TRẠNG THÁI

    @Test
    @DisplayName("Gọi cập nhật trạng thái phiên đấu giá thành công")
    void testUpdateAuctionStatus_ValidRequest_DelegatesToAuctionService() {
        UpdateAuctionStatusRequestDTO req = new UpdateAuctionStatusRequestDTO("auc123", AuctionStatus.ACTIVE);
        UpdateAuctionStatusResponseDTO expectedRes = new UpdateAuctionStatusResponseDTO(true, "Mở phiên thành công");
        when(auctionService.updateAuctionStatusByAdmin(req)).thenReturn(expectedRes);
        UpdateAuctionStatusResponseDTO actualRes = dispatcher.updateAuctionStatus(req);
        assertEquals(expectedRes, actualRes);
        verify(auctionService).updateAuctionStatusByAdmin(req);
    }

    // CÁC KỊCH BẢN KIỂM THỬ CHO ĐĂNG SẢN PHẨM (UPLOAD ITEM)

    @Test
    @DisplayName("Đăng sản phẩm thành công khi hồ sơ người bán đã được duyệt (REGISTERED)")
    void testUploadItem_ApprovedSeller_ReturnsSuccess() {
        UploadItemRequestDTO req = new UploadItemRequestDTO(
            "seller123",
            "Sản phẩm test",
            null,
            "Mô tả",
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            null,
            null,
            null,
            null,
            null
        );
        
        when(sellerProfileRepo.getSellerProfileStatus("seller123")).thenReturn(SellerRegisterStatus.REGISTERED.name());
        when(auctionService.uploadNewAuction(req)).thenReturn(true);

        UploadItemResponseDTO response = dispatcher.uploadItem(req);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Sản phẩm đã được đăng lên sàn đấu giá thành công!", response.getMessage());
        verify(sellerProfileRepo).getSellerProfileStatus("seller123");
        verify(auctionService).uploadNewAuction(req);
    }

    @Test
    @DisplayName("Đăng sản phẩm thất bại khi hồ sơ người bán chưa được duyệt hoặc bị chặn")
    void testUploadItem_NotApprovedSeller_ReturnsFailure() {
        UploadItemRequestDTO req = new UploadItemRequestDTO(
            "seller123",
            "Sản phẩm test",
            null,
            "Mô tả",
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            null,
            null,
            null,
            null,
            null
        );

        // Trường hợp UNREGISTERED
        when(sellerProfileRepo.getSellerProfileStatus("seller123")).thenReturn(SellerRegisterStatus.UNREGISTERED.name());
        UploadItemResponseDTO response1 = dispatcher.uploadItem(req);
        assertNotNull(response1);
        assertFalse(response1.isSuccess());
        assertEquals("Hồ sơ người bán của bạn chưa được duyệt hoặc đã bị chặn!", response1.getMessage());

        // Trường hợp DENIED
        when(sellerProfileRepo.getSellerProfileStatus("seller123")).thenReturn(SellerRegisterStatus.DENIED.name());
        UploadItemResponseDTO response2 = dispatcher.uploadItem(req);
        assertNotNull(response2);
        assertFalse(response2.isSuccess());
        assertEquals("Hồ sơ người bán của bạn chưa được duyệt hoặc đã bị chặn!", response2.getMessage());

        verify(sellerProfileRepo, times(2)).getSellerProfileStatus("seller123");
        verify(auctionService, never()).uploadNewAuction(any());
    }

    @Test
    @DisplayName("Đăng sản phẩm thất bại khi chưa đăng ký hồ sơ người bán")
    void testUploadItem_NoSellerProfile_ReturnsFailure() {
        UploadItemRequestDTO req = new UploadItemRequestDTO(
            "seller123",
            "Sản phẩm test",
            null,
            "Mô tả",
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            null,
            null,
            null,
            null,
            null
        );

        when(sellerProfileRepo.getSellerProfileStatus("seller123")).thenReturn(null);

        UploadItemResponseDTO response = dispatcher.uploadItem(req);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Bạn chưa có hồ sơ bán hàng! Vui lòng đăng ký trước khi đăng sản phẩm.", response.getMessage());
        verify(sellerProfileRepo).getSellerProfileStatus("seller123");
        verify(auctionService, never()).uploadNewAuction(any());
    }
}
