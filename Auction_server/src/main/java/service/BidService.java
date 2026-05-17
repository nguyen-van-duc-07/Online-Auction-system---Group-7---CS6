package service;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.request.PlaceBidRequestDTO;
import com.auction.shared.response.AuctionPriceUpdateDTO;
import com.auction.shared.response.NewBidDTO;
import com.auction.shared.response.PlaceBidResponseDTO;
import config.DatabaseConnection;
import repository.AuctionRepository;
import repository.BidTransactionRepository;
import scheduler.AutoBidProcessor;
import servercontroller.Server;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class BidService {
  private final AuctionRepository auctionRepo = new AuctionRepository();
  private final BidTransactionRepository bidRepo = new BidTransactionRepository();
  private final WalletService walletService = new WalletService();
  private static final BigDecimal FREEZE_RATE = new BigDecimal("0.1");

  public  PlaceBidResponseDTO placeBid(PlaceBidRequestDTO req) {
    Auction auction = auctionRepo.findAuctionById(req.getAuctionId());
    if (auction == null) {
      return new PlaceBidResponseDTO(
          false,
          "Auction không tồn tại"
      );
    }
    if (auction.getStatus() != AuctionStatus.ACTIVE) {
      return new PlaceBidResponseDTO(
          false,
          "Auction chưa mở hoặc đã đóng"
      );
    }
    BigDecimal minimumBid = auction.getCurrentHighestPrice().add(auction.getMinStepPrice());
    if (req.getBidAmount().compareTo(minimumBid) < 0) {
      return new PlaceBidResponseDTO(
          false,
          "Bid không hợp lệ");
    }
    // Đảm bảo cùng 1 connection
    try (Connection conn = DatabaseConnection.getConnection()) {
      // Đảm bảo khi 1 bước hỏng, cả quá trình sẽ ko lưu vào database nữa (đảm bảo tính thống nhất)
      conn.setAutoCommit(false); // Bắt đầu transaction
      try {
        // 2.1 Hoàn tiền (release) cho người đấu giá cao nhất hiện tại (nếu có)
        String currentHighestBidderId = auction.getHighestBidderId(); // Giả định hàm này tồn tại
        if (currentHighestBidderId != null && !currentHighestBidderId.isEmpty()) {
          walletService.releaseFrozen(
              conn,
              currentHighestBidderId,
              auction.getCurrentHighestPrice().multiply(FREEZE_RATE),
              req.getAuctionId()
          );
        }
        // 2.2 Đóng băng (freeze) tiền của người đấu giá mới
        walletService.freezeMoney(
            conn,
            req.getBidderId(),
            req.getBidAmount().multiply(FREEZE_RATE),
            req.getAuctionId()
        );
        BidTransaction bid = new BidTransaction(
            req.getAuctionId(),
            req.getBidderId(),
            req.getBidAmount()
        );
        boolean saved = bidRepo.saveBid(bid);
        System.out.println(
            "SAVE RESULT = " + saved
        );
        if (!saved) {
          conn.rollback();
          return new PlaceBidResponseDTO(
              false,
              "Không thể lưu bid"
          );
        }
        auctionRepo.updatePrice(
            req.getAuctionId(),
            req.getBidderId(),
            req.getBidAmount()
        );
        conn.commit(); // Lưu vào database
      } catch (Exception e) {
        conn.rollback();
        e.printStackTrace();
        return new PlaceBidResponseDTO(false, e.getMessage());
      } finally {
        conn.setAutoCommit(true); // Reset trạng thái connection
      }
    } catch (SQLException e) {
        return new PlaceBidResponseDTO(false, "Lỗi kết nối cơ sở dữ liệu");
    }
    // Sau khi thành công mới bắt đầu thông báo cho các clients
    Server.broadcastToAuctionRoom(
        new NewBidDTO(
            req.getAuctionId(),
            req.getBidderId(),
            req.getBidderName(),
            req.getBidAmount()
        ));
    Server.broadcastToAll(new AuctionPriceUpdateDTO(
        req.getAuctionId(),
        req.getBidAmount()
    ));
    new Thread(() ->
        AutoBidProcessor.process(req.getAuctionId(), req.getBidderId(), req.getBidAmount())
    ).start();
    return new PlaceBidResponseDTO(
        true,
        "Bid thành công"
    );
  }
}
