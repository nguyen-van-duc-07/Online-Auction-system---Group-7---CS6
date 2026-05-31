package servercontroller;

import com.auction.shared.request.*;
import com.auction.shared.response.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestHandlerTest {

    @Mock
    private RequestDispatcher mockDispatcher;

    @BeforeEach
    void setUp() {
        RequestHandler.setDispatcher(mockDispatcher);
    }

    @AfterEach
    void tearDown() {
        RequestHandler.setDispatcher(new RequestDispatcher());
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Login")
    void testLogin_ValidRequest_Success() {
        LoginRequestDTO req = mock(LoginRequestDTO.class);
        LoginResponseDTO res = mock(LoginResponseDTO.class);
        when(mockDispatcher.login(req)).thenReturn(res);
        assertEquals(res, RequestHandler.login(req));
        verify(mockDispatcher).login(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Signup")
    void testSignup_ValidRequest_Success() {
        SignUpRequestDTO req = mock(SignUpRequestDTO.class);
        SignUpResponseDTO res = mock(SignUpResponseDTO.class);
        when(mockDispatcher.signup(req)).thenReturn(res);
        assertEquals(res, RequestHandler.signup(req));
        verify(mockDispatcher).signup(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Upload Item")
    void testUploadItem_ValidRequest_Success() {
        UploadItemRequestDTO req = mock(UploadItemRequestDTO.class);
        UploadItemResponseDTO res = mock(UploadItemResponseDTO.class);
        when(mockDispatcher.uploadItem(req)).thenReturn(res);
        assertEquals(res, RequestHandler.uploadItem(req));
        verify(mockDispatcher).uploadItem(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Handle Find Auction By Id")
    void testHandleFindAuctionById_ValidRequest_Success() {
        AuctionRequestDTO req = mock(AuctionRequestDTO.class);
        AuctionResponseDTO res = mock(AuctionResponseDTO.class);
        when(mockDispatcher.handleFindAuctionById(req)).thenReturn(res);
        assertEquals(res, RequestHandler.handleFindAuctionById(req));
        verify(mockDispatcher).handleFindAuctionById(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Get Auctions By Status")
    void testGetAuctionsByStatus_ValidRequest_Success() {
        GetAuctionsRequestDTO req = mock(GetAuctionsRequestDTO.class);
        GetAuctionsResponseDTO res = mock(GetAuctionsResponseDTO.class);
        when(mockDispatcher.getAuctionsByStatus(req)).thenReturn(res);
        assertEquals(res, RequestHandler.getAuctionsByStatus(req));
        verify(mockDispatcher).getAuctionsByStatus(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Get Active And Waiting Auctions")
    void testGetActiveAndWaitingAuctions_ValidRequest_Success() {
        GetActiveAndWaitingAuctionsRequestDTO req = mock(GetActiveAndWaitingAuctionsRequestDTO.class);
        GetActiveAndWaitingAuctionsResponseDTO res = mock(GetActiveAndWaitingAuctionsResponseDTO.class);
        when(mockDispatcher.getActiveAndWaitingAuctions(req)).thenReturn(res);
        assertEquals(res, RequestHandler.getActiveAndWaitingAuctions(req));
        verify(mockDispatcher).getActiveAndWaitingAuctions(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Get Auctions By Seller")
    void testGetAuctionsBySeller_ValidRequest_Success() {
        GetAuctionsBySellerRequestDTO req = mock(GetAuctionsBySellerRequestDTO.class);
        GetAuctionsBySellerResponseDTO res = mock(GetAuctionsBySellerResponseDTO.class);
        when(mockDispatcher.getAuctionsForSeller(req)).thenReturn(res);
        assertEquals(res, RequestHandler.getAuctionsBySeller(req));
        verify(mockDispatcher).getAuctionsForSeller(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Update Profile")
    void testUpdateProfile_ValidRequest_Success() {
        UpdateProfileRequestDTO req = mock(UpdateProfileRequestDTO.class);
        UpdateProfileResponseDTO res = mock(UpdateProfileResponseDTO.class);
        when(mockDispatcher.updateProfile(req)).thenReturn(res);
        assertEquals(res, RequestHandler.updateProfile(req));
        verify(mockDispatcher).updateProfile(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Change Password")
    void testChangePassword_ValidRequest_Success() {
        ChangePasswordRequestDTO req = mock(ChangePasswordRequestDTO.class);
        ChangePasswordResponseDTO res = mock(ChangePasswordResponseDTO.class);
        when(mockDispatcher.changePassword(req)).thenReturn(res);
        assertEquals(res, RequestHandler.changePassword(req));
        verify(mockDispatcher).changePassword(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Place Bid")
    void testPlaceBid_ValidRequest_Success() {
        PlaceBidRequestDTO req = mock(PlaceBidRequestDTO.class);
        PlaceBidResponseDTO res = mock(PlaceBidResponseDTO.class);
        when(mockDispatcher.placeBid(req)).thenReturn(res);
        assertEquals(res, RequestHandler.placeBid(req));
        verify(mockDispatcher).placeBid(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Join Room")
    void testJoinRoom_ValidRequest_Success() {
        JoinRoomRequestDTO req = mock(JoinRoomRequestDTO.class);
        JoinRoomResponseDTO res = mock(JoinRoomResponseDTO.class);
        when(mockDispatcher.joinRoom(req, "u1")).thenReturn(res);
        assertEquals(res, RequestHandler.joinRoom(req, "u1"));
        verify(mockDispatcher).joinRoom(req, "u1");
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Seller Register")
    void testSellerRegister_ValidRequest_Success() {
        SellerRegisterRequestDTO req = mock(SellerRegisterRequestDTO.class);
        SellerRegisterResponseDTO res = mock(SellerRegisterResponseDTO.class);
        when(mockDispatcher.sellerRegister(req)).thenReturn(res);
        assertEquals(res, RequestHandler.sellerRegister(req));
        verify(mockDispatcher).sellerRegister(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Checking Seller Profile")
    void testCheckingSellerProfile_ValidRequest_Success() {
        CheckingSellerProfileRequestDTO req = mock(CheckingSellerProfileRequestDTO.class);
        CheckingSellerProfileResponseDTO res = mock(CheckingSellerProfileResponseDTO.class);
        when(mockDispatcher.checkingSellerProfile(req)).thenReturn(res);
        assertEquals(res, RequestHandler.checkingSellerProfile(req));
        verify(mockDispatcher).checkingSellerProfile(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Confirm Order")
    void testConfirmOrder_ValidRequest_Success() {
        ConfirmOrderRequestDTO req = mock(ConfirmOrderRequestDTO.class);
        OrderActionResponseDTO res = mock(OrderActionResponseDTO.class);
        when(mockDispatcher.confirmOrder(req)).thenReturn(res);
        assertEquals(res, RequestHandler.confirmOrder(req));
        verify(mockDispatcher).confirmOrder(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Cancel Order")
    void testCancelOrder_ValidRequest_Success() {
        CancelOrderRequestDTO req = mock(CancelOrderRequestDTO.class);
        OrderActionResponseDTO res = mock(OrderActionResponseDTO.class);
        when(mockDispatcher.cancelOrder(req)).thenReturn(res);
        assertEquals(res, RequestHandler.cancelOrder(req));
        verify(mockDispatcher).cancelOrder(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Get Order")
    void testGetOrder_ValidRequest_Success() {
        GetOrderRequestDTO req = mock(GetOrderRequestDTO.class);
        GetOrderResponseDTO res = mock(GetOrderResponseDTO.class);
        when(mockDispatcher.getOrder(req)).thenReturn(res);
        assertEquals(res, RequestHandler.getOrder(req));
        verify(mockDispatcher).getOrder(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Get Seller Profile")
    void testGetSellerProfile_ValidRequest_Success() {
        GetSellerProfileRequestDTO req = mock(GetSellerProfileRequestDTO.class);
        GetSellerProfileResponseDTO res = mock(GetSellerProfileResponseDTO.class);
        when(mockDispatcher.getSellerProfile(req)).thenReturn(res);
        assertEquals(res, RequestHandler.getSellerProfile(req));
        verify(mockDispatcher).getSellerProfile(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Update Seller Profile Status")
    void testUpdateSellerProfileStatus_ValidRequest_Success() {
        UpdateSellerProfileStatusRequestDTO req = mock(UpdateSellerProfileStatusRequestDTO.class);
        UpdateSellerProfileStatusResponseDTO res = mock(UpdateSellerProfileStatusResponseDTO.class);
        when(mockDispatcher.updateSellerProfileStatus(req)).thenReturn(res);
        assertEquals(res, RequestHandler.updateSellerProfileStatus(req));
        verify(mockDispatcher).updateSellerProfileStatus(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Cancel Seller Auctions")
    void testCancelSellerAuctions_ValidRequest_Success() {
        CancelSellerAuctionsRequestDTO req = mock(CancelSellerAuctionsRequestDTO.class);
        CancelSellerAuctionsResponseDTO res = mock(CancelSellerAuctionsResponseDTO.class);
        when(mockDispatcher.cancelSellerAuctions(req)).thenReturn(res);
        assertEquals(res, RequestHandler.cancelSellerAuctions(req));
        verify(mockDispatcher).cancelSellerAuctions(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Restore Seller Auctions")
    void testRestoreSellerAuctions_ValidRequest_Success() {
        RestoreSellerAuctionsRequestDTO req = mock(RestoreSellerAuctionsRequestDTO.class);
        RestoreSellerAuctionsResponseDTO res = mock(RestoreSellerAuctionsResponseDTO.class);
        when(mockDispatcher.restoreSellerAuctions(req)).thenReturn(res);
        assertEquals(res, RequestHandler.restoreSellerAuctions(req));
        verify(mockDispatcher).restoreSellerAuctions(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Set Auto Bid")
    void testSetAutoBid_ValidRequest_Success() {
        SetAutoBidRequestDTO req = mock(SetAutoBidRequestDTO.class);
        AutoBidResponseDTO res = mock(AutoBidResponseDTO.class);
        when(mockDispatcher.setAutoBid(req)).thenReturn(res);
        assertEquals(res, RequestHandler.setAutoBid(req));
        verify(mockDispatcher).setAutoBid(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Cancel Auto Bid")
    void testCancelAutoBid_ValidRequest_Success() {
        CancelAutoBidRequestDTO req = mock(CancelAutoBidRequestDTO.class);
        AutoBidResponseDTO res = mock(AutoBidResponseDTO.class);
        when(mockDispatcher.cancelAutoBid(req)).thenReturn(res);
        assertEquals(res, RequestHandler.cancelAutoBid(req));
        verify(mockDispatcher).cancelAutoBid(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Get Balance")
    void testGetBalance_ValidRequest_Success() {
        GetBalanceResponseDTO res = mock(GetBalanceResponseDTO.class);
        when(mockDispatcher.getBalance("u1")).thenReturn(res);
        assertEquals(res, RequestHandler.getBalance("u1"));
        verify(mockDispatcher).getBalance("u1");
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Get Notifications")
    void testGetNotifications_ValidRequest_Success() {
        GetNotificationsRequestDTO req = mock(GetNotificationsRequestDTO.class);
        GetNotificationsResponseDTO res = mock(GetNotificationsResponseDTO.class);
        when(mockDispatcher.getNotifications(req)).thenReturn(res);
        assertEquals(res, RequestHandler.getNotifications(req));
        verify(mockDispatcher).getNotifications(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Mark Notification Read")
    void testMarkNotificationRead_ValidRequest_Success() {
        MarkNotificationReadRequestDTO req = mock(MarkNotificationReadRequestDTO.class);
        RequestHandler.markNotificationRead(req);
        verify(mockDispatcher).markNotificationRead(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Handle Get Orders Of Seller")
    void testHandleGetOrdersOfSeller_ValidRequest_Success() {
        GetOrdersOfSellerRequestDTO req = mock(GetOrdersOfSellerRequestDTO.class);
        GetOrdersResponseDTO res = mock(GetOrdersResponseDTO.class);
        when(mockDispatcher.handleGetOrdersOfSeller(req)).thenReturn(res);
        assertEquals(res, RequestHandler.handleGetOrdersOfSeller(req));
        verify(mockDispatcher).handleGetOrdersOfSeller(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Handle Get Orders Of Buyer")
    void testHandleGetOrdersOfBuyer_ValidRequest_Success() {
        GetOrdersOfBuyerRequestDTO req = mock(GetOrdersOfBuyerRequestDTO.class);
        GetOrdersResponseDTO res = mock(GetOrdersResponseDTO.class);
        when(mockDispatcher.handleGetOrdersOfBuyer(req)).thenReturn(res);
        assertEquals(res, RequestHandler.handleGetOrdersOfBuyer(req));
        verify(mockDispatcher).handleGetOrdersOfBuyer(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Create Admin")
    void testCreateAdmin_ValidRequest_Success() {
        CreateAdminRequestDTO req = mock(CreateAdminRequestDTO.class);
        CreateAdminResponseDTO res = mock(CreateAdminResponseDTO.class);
        when(mockDispatcher.createAdmin(req)).thenReturn(res);
        assertEquals(res, RequestHandler.createAdmin(req));
        verify(mockDispatcher).createAdmin(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Create Transaction Request")
    void testCreateTransactionRequest_ValidRequest_Success() {
        CreateTransactionRequestDTO req = mock(CreateTransactionRequestDTO.class);
        CreateTransactionResponseDTO res = mock(CreateTransactionResponseDTO.class);
        when(mockDispatcher.createTransactionRequest(req)).thenReturn(res);
        assertEquals(res, RequestHandler.createTransactionRequest(req));
        verify(mockDispatcher).createTransactionRequest(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Get Pending Transactions")
    void testGetPendingTransactions_ValidRequest_Success() {
        GetPendingTransactionsRequestDTO req = mock(GetPendingTransactionsRequestDTO.class);
        GetPendingTransactionsResponseDTO res = mock(GetPendingTransactionsResponseDTO.class);
        when(mockDispatcher.getPendingTransactions(req)).thenReturn(res);
        assertEquals(res, RequestHandler.getPendingTransactions(req));
        verify(mockDispatcher).getPendingTransactions(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Process Transaction Request")
    void testProcessTransactionRequest_ValidRequest_Success() {
        ProcessTransactionRequestDTO req = mock(ProcessTransactionRequestDTO.class);
        ProcessTransactionResponseDTO res = mock(ProcessTransactionResponseDTO.class);
        when(mockDispatcher.processTransactionRequest(req)).thenReturn(res);
        assertEquals(res, RequestHandler.processTransactionRequest(req));
        verify(mockDispatcher).processTransactionRequest(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Update Auction Status")
    void testUpdateAuctionStatus_ValidRequest_Success() {
        UpdateAuctionStatusRequestDTO req = mock(UpdateAuctionStatusRequestDTO.class);
        UpdateAuctionStatusResponseDTO res = mock(UpdateAuctionStatusResponseDTO.class);
        when(mockDispatcher.updateAuctionStatus(req)).thenReturn(res);
        assertEquals(res, RequestHandler.updateAuctionStatus(req));
        verify(mockDispatcher).updateAuctionStatus(req);
    }

    @Test
    @DisplayName("Kiểm tra xử lý request Get All Users")
    void testGetAllUsers_ValidRequest_Success() {
        GetAllUsersRequestDTO req = mock(GetAllUsersRequestDTO.class);
        GetAllUsersResponseDTO res = mock(GetAllUsersResponseDTO.class);
        when(mockDispatcher.getAllUsers(req)).thenReturn(res);
        assertEquals(res, RequestHandler.getAllUsers(req));
        verify(mockDispatcher).getAllUsers(req);
    }
}
