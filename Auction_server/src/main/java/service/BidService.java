package service;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.request.PlaceBidRequestDTO;
import com.auction.shared.response.NewBidDTO;
import com.auction.shared.response.PlaceBidResponseDTO;
import repository.AuctionRepository;
import repository.BidTransactionRepository;
import servercontroller.Server;
import java.math.BigDecimal;

public class BidService {
  private final AuctionRepository auctionRepo = new AuctionRepository();
  private final BidTransactionRepository bidRepo = new BidTransactionRepository();

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
    Server.broadcastToAuctionRoom(
        new NewBidDTO(
            req.getAuctionId(),
            req.getBidderId(),
            req.getBidderName(),
            req.getBidAmount()
        )
    );
    return new PlaceBidResponseDTO(
        true,
        "Bid thành công"
    );
  }
}
