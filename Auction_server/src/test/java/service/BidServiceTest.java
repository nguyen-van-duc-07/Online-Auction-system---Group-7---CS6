package service;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.auction.AutoBidConfig;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.model.user.Wallet;
import com.auction.shared.request.PlaceBidRequestDTO;
import com.auction.shared.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.*;
import config.ConnectionProvider;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BidServiceTest {

    @Mock
    private AuctionRepository auctionRepo;

    @Mock
    private BidTransactionRepository bidRepo;

    @Mock
    private AutoBidConfigRepository autoBidRepo;

    @Mock
    private WalletService walletService;

    @Mock
    private WalletRepository walletRepo;


    @Mock
    private UserRepository userRepo;

    @Mock
    private ConnectionProvider connectionProvider;

    @Mock
    private Connection mockConnection;

    private BidService bidService;

    @BeforeEach
    void setUp() {
        bidService = new BidService(
            auctionRepo,
            bidRepo,
            autoBidRepo,
            walletService,
            walletRepo,
            userRepo,
            connectionProvider
        );
    }

    // ==========================================
    // TEST CASES FOR placeBid (Manual Bidding)
    // ==========================================

    @Test
    void placeBid_auctionNotFound_returnsFailure() throws Exception {
        // Arrange
        PlaceBidRequestDTO req = new PlaceBidRequestDTO("auc123", "bidder456", "John", new BigDecimal("1000.00"));
        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.findAuctionForUpdate(mockConnection, "auc123")).thenReturn(null);

        // Act
        PlaceBidResponseDTO resp = bidService.placeBid(req);

        // Assert
        assertFalse(resp.isSuccess());
        assertEquals("Auction không tồn tại", resp.getMessage());
        verify(mockConnection).rollback();
    }

    @Test
    void placeBid_auctionNotActive_returnsFailure() throws Exception {
        // Arrange
        PlaceBidRequestDTO req = new PlaceBidRequestDTO("auc123", "bidder456", "John", new BigDecimal("1000.00"));
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setStatus(AuctionStatus.WAITING);

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.findAuctionForUpdate(mockConnection, "auc123")).thenReturn(auction);

        // Act
        PlaceBidResponseDTO resp = bidService.placeBid(req);

        // Assert
        assertFalse(resp.isSuccess());
        assertEquals("Auction chưa mở hoặc đã đóng", resp.getMessage());
        verify(mockConnection).rollback();
    }

    @Test
    void placeBid_auctionExpired_returnsFailure() throws Exception {
        // Arrange
        PlaceBidRequestDTO req = new PlaceBidRequestDTO("auc123", "bidder456", "John", new BigDecimal("1000.00"));
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setEndTime(LocalDateTime.now().minusMinutes(1)); // Expired

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.findAuctionForUpdate(mockConnection, "auc123")).thenReturn(auction);

        // Act
        PlaceBidResponseDTO resp = bidService.placeBid(req);

        // Assert
        assertFalse(resp.isSuccess());
        assertEquals("Phiên đấu giá đã hết thời gian", resp.getMessage());
        verify(mockConnection).rollback();
    }

    @Test
    void placeBid_sellerBidsOwnAuction_returnsFailure() throws Exception {
        // Arrange
        String sellerId = "seller789";
        PlaceBidRequestDTO req = new PlaceBidRequestDTO("auc123", sellerId, "John", new BigDecimal("1000.00"));
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setEndTime(LocalDateTime.now().plusMinutes(10));
        auction.setUserId(sellerId); // Same as bidderId

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.findAuctionForUpdate(mockConnection, "auc123")).thenReturn(auction);

        // Act
        PlaceBidResponseDTO resp = bidService.placeBid(req);

        // Assert
        assertFalse(resp.isSuccess());
        assertEquals("Người bán không thể tự đấu giá sản phẩm của mình", resp.getMessage());
        verify(mockConnection).rollback();
    }

    @Test
    void placeBid_bidAmountLessThanMinimum_returnsFailure() throws Exception {
        // Arrange
        PlaceBidRequestDTO req = new PlaceBidRequestDTO("auc123", "bidder456", "John", new BigDecimal("1050.00"));
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setEndTime(LocalDateTime.now().plusMinutes(10));
        auction.setUserId("seller789");
        auction.setCurrentHighestPrice(new BigDecimal("1000.00"));
        auction.setMinStepPrice(new BigDecimal("100.00")); // Minimum bid must be 1100.00

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.findAuctionForUpdate(mockConnection, "auc123")).thenReturn(auction);

        // Act
        PlaceBidResponseDTO resp = bidService.placeBid(req);

        // Assert
        assertFalse(resp.isSuccess());
        assertEquals("Bid không hợp lệ", resp.getMessage());
        verify(mockConnection).rollback();
    }

    @Test
    void placeBid_validBid_savesBidUpdatesPriceAndTransfersDeposit() throws Exception {
        // Arrange
        PlaceBidRequestDTO req = new PlaceBidRequestDTO("auc123", "bidder456", "John", new BigDecimal("1200.00"));
        
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId("auc123");
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setEndTime(LocalDateTime.now().plusMinutes(10));
        auction.setUserId("seller789");
        auction.setCurrentHighestPrice(new BigDecimal("1000.00"));
        auction.setMinStepPrice(new BigDecimal("100.00"));
        auction.setHighestBidderId("oldBidder");

        Wallet oldWallet = new Wallet();
        oldWallet.setFrozenBalance(new BigDecimal("100.00"));
        when(walletRepo.getWalletByUserIdForUpdate(mockConnection, "oldBidder")).thenReturn(oldWallet);

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.findAuctionForUpdate(mockConnection, "auc123")).thenReturn(auction);
        when(bidRepo.saveBid(eq(mockConnection), any(BidTransaction.class))).thenReturn(true);
        when(autoBidRepo.findActiveBotsOrderedByMaxPrice(mockConnection, "auc123")).thenReturn(Collections.emptyList());

        // Act
        PlaceBidResponseDTO resp = bidService.placeBid(req);

        // Assert
        assertTrue(resp.isSuccess());
        assertEquals("Bid thành công", resp.getMessage());
        verify(walletService).releaseFrozen(eq(mockConnection), eq("oldBidder"), argThat(val -> val.compareTo(new BigDecimal("100.00")) == 0), eq("auc123"));
        verify(walletService).freezeMoney(eq(mockConnection), eq("bidder456"), argThat(val -> val.compareTo(new BigDecimal("120.00")) == 0), eq("auc123"));
        verify(bidRepo).saveBid(eq(mockConnection), any(BidTransaction.class));
        verify(auctionRepo).updatePrice(mockConnection, "auc123", "bidder456", new BigDecimal("1200.00"));
        verify(mockConnection).commit();
    }

    // ==========================================
    // TEST CASES FOR applyAntiSniping
    // ==========================================

    @Test
    void placeBid_inAntiSnipingRange_extendsEndTime() throws Exception {
        // Arrange
        PlaceBidRequestDTO req = new PlaceBidRequestDTO("auc123", "bidder456", "John", new BigDecimal("1200.00"));
        
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId("auc123");
        auction.setStatus(AuctionStatus.ACTIVE);
        // Anti-snipe threshold is 3 minutes. Set end time to 1 minute from now to trigger extension.
        LocalDateTime originalEndTime = LocalDateTime.now().plusMinutes(1);
        auction.setEndTime(originalEndTime);
        auction.setUserId("seller789");
        auction.setCurrentHighestPrice(new BigDecimal("1000.00"));
        auction.setMinStepPrice(new BigDecimal("100.00"));
        auction.setHighestBidderId(null);

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.findAuctionForUpdate(mockConnection, "auc123")).thenReturn(auction);
        when(bidRepo.saveBid(eq(mockConnection), any(BidTransaction.class))).thenReturn(true);
        when(autoBidRepo.findActiveBotsOrderedByMaxPrice(mockConnection, "auc123")).thenReturn(Collections.emptyList());

        // Act
        PlaceBidResponseDTO resp = bidService.placeBid(req);

        // Assert
        assertTrue(resp.isSuccess());
        assertNotEquals(originalEndTime, auction.getEndTime());
        // Extended by 3 minutes
        assertTrue(auction.getEndTime().isAfter(originalEndTime));
        verify(auctionRepo).updateEndTime(eq(mockConnection), eq("auc123"), any(LocalDateTime.class));
    }

    // ==========================================
    // TEST CASES FOR resolveAutoBidFight
    // ==========================================

    @Test
    void resolveAutoBidFight_singleBot_bidsSuccessfully() throws Exception {
        // Arrange
        String auctionId = "auc123";
        AuctionResponseDTO auction = new AuctionResponseDTO();
        auction.setId(auctionId);
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setCurrentHighestPrice(new BigDecimal("1000.00"));
        auction.setMinStepPrice(new BigDecimal("100.00"));
        auction.setHighestBidderId("oldBidder");

        AutoBidConfig bot = new AutoBidConfig("botUser", auctionId, new BigDecimal("2000.00"), new BigDecimal("150.00"));
        List<AutoBidConfig> bots = new ArrayList<>();
        bots.add(bot);

        Wallet wallet = new Wallet();
        wallet.setBalance(new BigDecimal("1000.00"));

        Wallet oldWallet = new Wallet();
        oldWallet.setFrozenBalance(new BigDecimal("100.00"));
        when(walletRepo.getWalletByUserIdForUpdate(mockConnection, "oldBidder")).thenReturn(oldWallet);

        when(connectionProvider.getConnection()).thenReturn(mockConnection);
        when(auctionRepo.findAuctionForUpdate(mockConnection, auctionId)).thenReturn(auction);
        when(autoBidRepo.findActiveBotsOrderedByMaxPrice(mockConnection, auctionId)).thenReturn(bots);
        when(walletRepo.getWalletByUserIdForUpdate(mockConnection, "botUser")).thenReturn(wallet);
        when(userRepo.getAccountNameByUserId("botUser")).thenReturn("AutoBot");

        // Act
        bidService.resolveAutoBidFight(auctionId);

        // Assert
        // Top price should be: currentHighestPrice (1000) + step (150) = 1150
        assertEquals(new BigDecimal("1150.00"), auction.getCurrentHighestPrice());
        assertEquals("botUser", auction.getHighestBidderId());
        verify(walletService).releaseFrozen(eq(mockConnection), eq("oldBidder"), argThat(val -> val.compareTo(new BigDecimal("100.00")) == 0), eq(auctionId));
        verify(walletService).freezeMoney(eq(mockConnection), eq("botUser"), argThat(val -> val.compareTo(new BigDecimal("115.00")) == 0), eq(auctionId)); // 10% of 1150
        verify(bidRepo).saveBid(eq(mockConnection), any(BidTransaction.class));
        verify(auctionRepo).updatePrice(mockConnection, auctionId, "botUser", new BigDecimal("1150.00"));
        verify(mockConnection).commit();
    }
}
