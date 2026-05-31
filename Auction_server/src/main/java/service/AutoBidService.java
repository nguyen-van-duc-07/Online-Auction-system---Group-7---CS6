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
  private final AutoBidConfigRepository configRepo;
  private final AuctionRepository auctionRepo;

  private static class Holder {
    private static final AutoBidService INSTANCE = new AutoBidService();
  }

  public static AutoBidService getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * Constructor mặc định cho môi trường Production.
   */
  private AutoBidService() {
    this(AutoBidConfigRepository.getInstance(), AuctionRepository.getInstance());
  }

  /**
   * Constructor nhận tham số phục vụ cho Unit Test.
   */
  public AutoBidService(AutoBidConfigRepository configRepo, AuctionRepository auctionRepo) {
    this.configRepo = configRepo;
    this.auctionRepo = auctionRepo;
  }

  public boolean setAutoBid(SetAutoBidRequestDTO req) {
    if (!req.isActive()) {
      return configRepo.deactivate(req.getUserId(), req.getAuctionId());
    }

    // Xác thực thông tin phiên đấu giá
    AuctionResponseDTO auction = auctionRepo.findAuctionResponseDTOById(req.getAuctionId());
    if (auction == null) return false;

    // Bước giá tự động (stepAmount) không được nhỏ hơn bước giá tối thiểu (minStepPrice)
    if (req.getStepAmount().compareTo(auction.getMinStepPrice()) < 0) {
      return false;
    }

    // Giá tối đa (maxPrice) phải lớn hơn giá hiện tại
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
    return configRepo.deactivate(req.getUserId(), req.getAuctionId());
  }
}
