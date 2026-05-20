package service;

import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.auction.AutoBidConfig;
import com.auction.shared.request.CancelAutoBidRequestDTO;
import com.auction.shared.request.SetAutoBidRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import repository.AuctionRepository;
import repository.AutoBidConfigRepository;
import com.auction.shared.model.auction.Auction;

import java.math.BigDecimal;

public class AutoBidService {
  private final AutoBidConfigRepository configRepo = new AutoBidConfigRepository();
  private final AuctionRepository auctionRepo = new AuctionRepository();

  public boolean setAutoBid(SetAutoBidRequestDTO req) {
    // Validate
    AuctionResponseDTO auction = auctionRepo.findAuctionById(req.getAuctionId());
    if (auction == null) return false;

    // stepAmount không được nhỏ hơn minStepPrice
    if (req.getStepAmount().compareTo(auction.getMinStepPrice()) < 0) {
      return false;
    }

    // maxPrice phải lớn hơn giá hiện tại
    if (req.getMaxPrice().compareTo(auction.getCurrentHighestPrice()) <= 0) {
      return false;
    }

    AutoBidConfig config = new AutoBidConfig(
        req.getUserId(),
        req.getAuctionId(),
        req.getMaxPrice(),
        req.getStepAmount()
    );
    return configRepo.save(config);
  }

  public boolean cancelAutoBid(CancelAutoBidRequestDTO req) {
    configRepo.deactivate(req.getUserId(), req.getAuctionId());
    return true;
  }
}