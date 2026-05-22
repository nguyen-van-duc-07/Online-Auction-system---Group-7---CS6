package service;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.auction.AutoBidConfig;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.model.user.Wallet;
import com.auction.shared.request.PlaceBidRequestDTO;
import com.auction.shared.response.AuctionPriceUpdateDTO;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.response.AutoBidDefeatedDTO;
import com.auction.shared.response.NewBidDTO;
import com.auction.shared.response.PlaceBidResponseDTO;
import config.DatabaseConnection;
import repository.AuctionRepository;
import repository.AutoBidConfigRepository;
import repository.BidTransactionRepository;
import repository.UserRepository;
import repository.WalletRepository;
import servercontroller.Server;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BidService {
  private final AuctionRepository auctionRepo = new AuctionRepository();
  private final BidTransactionRepository bidRepo = new BidTransactionRepository();
  private final AutoBidConfigRepository autoBidRepo = new AutoBidConfigRepository();
  private final WalletService walletService = new WalletService();
  private final WalletRepository walletRepo = new WalletRepository();

  private static final BigDecimal FREEZE_RATE = new BigDecimal("0.1");

  public  PlaceBidResponseDTO placeBid(PlaceBidRequestDTO req) {
    AuctionResponseDTO auction = auctionRepo.findAuctionResponseDTOById(req.getAuctionId());
    if (auction == null) {
      return new PlaceBidResponseDTO(false, "Auction không tồn tại");
    }
    if (auction.getStatus() != AuctionStatus.ACTIVE) {
      return new PlaceBidResponseDTO(false, "Auction chưa mở hoặc đã đóng");
    }
    BigDecimal minimumBid = auction.getCurrentHighestPrice().add(auction.getMinStepPrice());
    if (req.getBidAmount().compareTo(minimumBid) < 0) {
      return new PlaceBidResponseDTO(false, "Bid không hợp lệ");
    }

    // ================= LUỒNG 1: XỬ LÝ ĐẶT GIÁ CỦA NGƯỜI THẬT =================
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        // Hoàn tiền cho người cũ
        String currentHighestBidderId = auction.getHighestBidderId();
        if (currentHighestBidderId != null && !currentHighestBidderId.isEmpty()) {
          walletService.releaseFrozen(conn, currentHighestBidderId,
                  auction.getCurrentHighestPrice().multiply(FREEZE_RATE), req.getAuctionId());
        }

        // Đóng băng tiền người mới
        walletService.freezeMoney(conn, req.getBidderId(),
                req.getBidAmount().multiply(FREEZE_RATE), req.getAuctionId());

        BidTransaction bid = new BidTransaction(req.getAuctionId(), req.getBidderId(), req.getBidAmount());
        boolean saved = bidRepo.saveBid(bid);

        if (!saved) {
          conn.rollback();
          return new PlaceBidResponseDTO(false, "Không thể lưu bid");
        }

        auctionRepo.updatePrice(req.getAuctionId(), req.getBidderId(), req.getBidAmount());
        conn.commit();
      } catch (Exception e) {
        conn.rollback();
        e.printStackTrace();
        return new PlaceBidResponseDTO(false, e.getMessage());
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      return new PlaceBidResponseDTO(false, "Lỗi kết nối cơ sở dữ liệu");
    }

    // Phát sóng giao dịch thủ công thành công
    Server.broadcastToAuctionRoom(new NewBidDTO(req.getAuctionId(), req.getBidderId(), req.getBidderName(), req.getBidAmount()));
    Server.broadcastToAll(new AuctionPriceUpdateDTO(req.getAuctionId(), req.getBidAmount()));

    // ================= LUỒNG 2: JUMP CALCULATION (BOT PHẢN ĐÒN O(1)) =================
    // Đã XÓA Thread gọi sang AutoBidProcessor cũ. Thay bằng hàm bên dưới.
    handleBotCounterAttack(req.getAuctionId(), req.getBidderId(), req.getBidAmount(), auction.getMinStepPrice());

    return new PlaceBidResponseDTO(true, "Bid thành công");
  }

  /**
   * Thuật toán O(1) xử lý Bot phản đòn, kế thừa tính năng kiểm tra ví 10% của hệ thống.
   */
  private void handleBotCounterAttack(String auctionId, String manualBidderId, BigDecimal manualBidAmount, BigDecimal stepAmount) {
    List<AutoBidConfig> activeBots = autoBidRepo.findActiveBotsOrderedByMaxPrice(auctionId);

    // Nếu không có Bot hoặc người vừa đấu giá chính là chủ của Bot top 1
    if (activeBots.isEmpty() || activeBots.get(0).getUserId().equals(manualBidderId)) {
      return;
    }

    AutoBidConfig bot = activeBots.get(0);

    // KỊCH BẢN A: BOT CÒN NGÂN SÁCH (Max Price > Giá người thật)
    if (bot.getMaxPrice().compareTo(manualBidAmount) > 0) {

      BigDecimal priceToBeat = manualBidAmount.add(stepAmount);
      BigDecimal newBotPrice = priceToBeat.min(bot.getMaxPrice());
      BigDecimal requiredFreeze = newBotPrice.multiply(FREEZE_RATE);

      Wallet botWallet = walletRepo.getWalletByUserId(bot.getUserId());

      // Kiểm tra xem ví Bot có đủ 10% không
      if (botWallet != null && botWallet.getBalance().compareTo(requiredFreeze) >= 0) {

        // Ví đủ tiền -> Mở SQL Transaction chốt giao dịch cho Bot
        try (Connection conn = DatabaseConnection.getConnection()) {
          conn.setAutoCommit(false);
          try {
            // 1. Hoàn 10% tiền cho người thật vừa bị đè
            walletService.releaseFrozen(conn, manualBidderId, manualBidAmount.multiply(FREEZE_RATE), auctionId);

            // 2. Đóng băng 10% tiền của Bot
            walletService.freezeMoney(conn, bot.getUserId(), requiredFreeze, auctionId);

            // 3. Cập nhật DB
            bidRepo.saveBid(new BidTransaction(auctionId, bot.getUserId(), newBotPrice));
            auctionRepo.updatePrice(auctionId, bot.getUserId(), newBotPrice);

            conn.commit();

            // 4. Phát sóng giá mới của Bot
            String botName = UserRepository.getUserFullName(bot.getUserId());
            Server.broadcastToAuctionRoom(new NewBidDTO(auctionId, bot.getUserId(), "[Auto] " + botName, newBotPrice));
            Server.broadcastToAll(new AuctionPriceUpdateDTO(auctionId, newBotPrice));

          } catch (Exception e) {
            conn.rollback();
            e.printStackTrace();
          } finally {
            conn.setAutoCommit(true);
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      // Ví KHÔNG ĐỦ 10% -> Giết Bot
      else {
        autoBidRepo.deactivate(bot.getUserId(), auctionId);
        String fomoMessage = "Bot đã tự tắt do số dư ví không đủ 10% (" + requiredFreeze + " VNĐ) để tiếp tục đấu giá!";
        Server.sendToUser(bot.getUserId(), new AutoBidDefeatedDTO(auctionId, fomoMessage));
      }
    }
    // KỊCH BẢN B: BOT HẾT NGÂN SÁCH (Người thật trả giá vượt quá Max Price của Bot)
    else {
      autoBidRepo.deactivate(bot.getUserId(), auctionId);
      String fomoMessage = "Một tài phiệt khác vừa trả giá đè bẹp ngân sách Bot của bạn!";
      Server.sendToUser(bot.getUserId(), new AutoBidDefeatedDTO(auctionId, fomoMessage));
    }
  }
}