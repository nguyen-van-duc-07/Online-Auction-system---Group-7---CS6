package servercontroller;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.User;
import com.auction.shared.request.*;
import com.auction.shared.response.*;
import org.junit.jupiter.api.BeforeEach;
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

    // ==========================================
    // CÁC KỊCH BẢN KIỂM THỬ CHO ĐĂNG NHẬP
    // ==========================================

    @Test
    void login_hopLe_traVeThanhCong() {
        // Sắp đặt
        LoginRequestDTO req = new LoginRequestDTO("0987654321", "pass123");
        Bidder bidder = new Bidder();
        bidder.setId("user123");
        bidder.setRole(UserRole.BIDDER);
        bidder.setAccountName("Nguyen Van A");

        when(authService.login(req)).thenReturn(bidder);

        // Thực thi
        LoginResponseDTO response = dispatcher.login(req);

        // Xác minh
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Đăng nhập thành công!", response.getMessage());
        assertEquals("user123", response.getUser().getId());
        verify(authService).login(req);
    }

    @Test
    void login_saiThongTin_traVeThatBai() {
        // Sắp đặt
        LoginRequestDTO req = new LoginRequestDTO("0987654321", "wrongPass");
        when(authService.login(req)).thenReturn(null);

        // Thực thi
        LoginResponseDTO response = dispatcher.login(req);

        // Xác minh
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Sai tài khoản hoặc mật khẩu", response.getMessage());
        verify(authService).login(req);
    }

    // ==========================================
    // CÁC KỊCH BẢN KIỂM THỬ CHO ĐẶT GIÁ
    // ==========================================

    @Test
    void placeBid_goiDenBidService() {
        // Sắp đặt
        PlaceBidRequestDTO req = new PlaceBidRequestDTO("auc123", "bidder123", "Bidder Name", new BigDecimal("1500000.00"));
        PlaceBidResponseDTO expectedResponse = new PlaceBidResponseDTO(true, "Đặt giá thành công");
        when(bidService.placeBid(req)).thenReturn(expectedResponse);

        // Thực thi
        PlaceBidResponseDTO actualResponse = dispatcher.placeBid(req);

        // Xác minh
        assertEquals(expectedResponse, actualResponse);
        verify(bidService).placeBid(req);
    }

    // ==========================================
    // CÁC KỊCH BẢN KIỂM THỬ CHO SỐ DƯ VÍ
    // ==========================================

    @Test
    void getBalance_hopLe_goiWalletService() throws Exception {
        // Sắp đặt
        String userId = "user123";
        BigDecimal expectedBalance = new BigDecimal("5000000.00");
        when(walletService.getBalance(userId)).thenReturn(expectedBalance);

        // Thực thi
        GetBalanceResponseDTO response = dispatcher.getBalance(userId);

        // Xác minh
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(expectedBalance, response.getBalance());
        verify(walletService).getBalance(userId);
    }

    @Test
    void getBalance_gapNgoaiLe_traVeZero() throws Exception {
        // Sắp đặt
        String userId = "user123";
        when(walletService.getBalance(userId)).thenThrow(new RuntimeException("Lỗi kết nối DB"));

        // Thực thi
        GetBalanceResponseDTO response = dispatcher.getBalance(userId);

        // Xác minh
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        verify(walletService).getBalance(userId);
    }

    // ==========================================
    // CÁC KỊCH BẢN KIỂM THỬ CHO ADMIN CẬP NHẬT TRẠNG THÁI
    // ==========================================

    @Test
    void updateAuctionStatus_goiAuctionService() {
        // Sắp đặt
        UpdateAuctionStatusRequestDTO req = new UpdateAuctionStatusRequestDTO("auc123", AuctionStatus.ACTIVE);
        UpdateAuctionStatusResponseDTO expectedRes = new UpdateAuctionStatusResponseDTO(true, "Mở phiên thành công");
        when(auctionService.updateAuctionStatusByAdmin(req)).thenReturn(expectedRes);

        // Thực thi
        UpdateAuctionStatusResponseDTO actualRes = dispatcher.updateAuctionStatus(req);

        // Xác minh
        assertEquals(expectedRes, actualRes);
        verify(auctionService).updateAuctionStatusByAdmin(req);
    }
}
