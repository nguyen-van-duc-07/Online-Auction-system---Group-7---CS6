package scheduler;

import com.auction.shared.model.auction.AutoBidConfig;
import com.auction.shared.model.user.Wallet;
import com.auction.shared.request.PlaceBidRequestDTO;
import com.auction.shared.response.PlaceBidResponseDTO;
import repository.AutoBidConfigRepository;
import repository.AuctionRepository;
import repository.WalletRepository;
import service.BidService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class AutoBidProcessor {
  private static final AutoBidConfigRepository configRepo = new AutoBidConfigRepository();
  private static final AuctionRepository auctionRepo = new AuctionRepository();
  private static final BidService bidService = new BidService();
  private static final WalletRepository walletRepo = new WalletRepository();

  // Lock rieng cho tung auction, tranh viec nhieu thread cung xu ly 1 auction
  private static final Map<String, Object> locks = new ConcurrentHashMap<>();

  public static void process(String auctionId, String lastBidderId, BigDecimal currentPrice) {
    // Lay lock cu neu da co hoac tao lock moi
    Object lock = locks.computeIfAbsent(auctionId, k -> new Object());

    synchronized (lock) {
      List<AutoBidConfig> configs =
          configRepo.findActiveByAuctionId(auctionId, lastBidderId);

      if (configs.isEmpty()) return;

      // Lấy config có maxPrice cao nhất (đã sort DESC trong query)
      AutoBidConfig winner = configs.get(0);

      BigDecimal nextBid = currentPrice.add(winner.getStepAmount());

      // Kiểm tra có vượt max không
      if (nextBid.compareTo(winner.getMaxPrice()) > 0) {
        // Không đủ tiền để bid thêm → tắt autobid
        configRepo.deactivate(winner.getUserId(), auctionId);
        System.out.println("[AUTOBID] User " + winner.getUserId()
            + " đã đạt giới hạn max, tắt autobid.");
        return;
      }

      Wallet wallet = walletRepo.getWalletByUserId(winner.getUserId());
      BigDecimal requiredFreeze = nextBid.multiply(new BigDecimal("0.1"));
      if (wallet == null || wallet.getBalance().compareTo(requiredFreeze) < 0) {
        configRepo.deactivate(winner.getUserId(), auctionId);
        System.out.println("[AUTOBID] User " + winner.getUserId()
            + " không đủ số dư, tắt autobid.");
        return;
      }

      // Thực hiện bid
      System.out.println("[AUTOBID] User " + winner.getUserId()
          + " tự động bid: " + nextBid);

      PlaceBidRequestDTO autoBidReq = new PlaceBidRequestDTO(
          auctionId,
          winner.getUserId(),
          "[Auto]",   // bidderName
          nextBid
      );

      PlaceBidResponseDTO result = bidService.placeBid(autoBidReq);
      if (!result.isSuccess()) {
        // Bid thất bại vì lý do khác → tắt autobid để tránh lặp
        configRepo.deactivate(winner.getUserId(), auctionId);
        System.out.println("[AUTOBID] Bid thất bại, tắt autobid: " + result.getMessage());
      }
    }
  }
}