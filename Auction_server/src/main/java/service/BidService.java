package service;

import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.model.auction.AutoBidConfig;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.model.user.Wallet;
import com.auction.shared.request.PlaceBidRequestDTO;
import com.auction.shared.response.*;
import config.AuctionConfig;
import config.DatabaseConnection;
import repository.AuctionRepository;
import repository.AutoBidConfigRepository;
import repository.BidTransactionRepository;
import repository.SellerProfileRepository;
import repository.UserRepository;
import repository.WalletRepository;
import servercontroller.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BidService {
  private static final Logger log = LoggerFactory.getLogger(BidService.class);
  private final AuctionRepository auctionRepo = new AuctionRepository();
  private final BidTransactionRepository bidRepo = new BidTransactionRepository();
  private final AutoBidConfigRepository autoBidRepo = new AutoBidConfigRepository();
  private final WalletService walletService = new WalletService();
  private final WalletRepository walletRepo = new WalletRepository();
  private final SellerProfileRepository sellerProfileRepo = new SellerProfileRepository();
  private final UserRepository userRepo = new UserRepository();

  private static final BigDecimal FREEZE_RATE = new BigDecimal("0.1");
  private static final ConcurrentHashMap<String, Object> auctionLocks = new ConcurrentHashMap<>();

  /**
   * Khóa theo phiên — dùng cho mọi thao tác ghi auctions (bid, resolve auto-bid fight).
   */
  public static void runWithAuctionLock(String auctionId, Runnable action) {
    Object auctionLock = auctionLocks.computeIfAbsent(auctionId, k -> new Object());
    synchronized (auctionLock) {
      action.run();
    }
  }

  public PlaceBidResponseDTO placeBid(PlaceBidRequestDTO req) {
    String auctionId = req.getAuctionId();
    PostCommitEvents events = new PostCommitEvents();

    PlaceBidResponseDTO[] response = new PlaceBidResponseDTO[1];
    runWithAuctionLock(auctionId, () -> response[0] = placeBidWithinLock(req, events));
    return response[0];
  }

  private PlaceBidResponseDTO placeBidWithinLock(PlaceBidRequestDTO req, PostCommitEvents events) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        AuctionResponseDTO auction = auctionRepo.findAuctionForUpdate(conn, req.getAuctionId());
        if (auction == null) {
          conn.rollback();
          return new PlaceBidResponseDTO(false, "Auction không tồn tại");
        }
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
          conn.rollback();
          return new PlaceBidResponseDTO(false, "Auction chưa mở hoặc đã đóng");
        }

        LocalDateTime now = LocalDateTime.now();
        if (auction.getEndTime() != null && !now.isBefore(auction.getEndTime())) {
          conn.rollback();
          return new PlaceBidResponseDTO(false, "Phiên đấu giá đã hết thời gian");
        }

        String bidderSellerProfileId = sellerProfileRepo.findProfileIdByUserId(req.getBidderId());
        if (bidderSellerProfileId != null && bidderSellerProfileId.equals(auction.getSellerId())) {
          conn.rollback();
          return new PlaceBidResponseDTO(false, "Người bán không thể tự đấu giá sản phẩm của mình");
        }

        BigDecimal minimumBid = auction.getCurrentHighestPrice().add(auction.getMinStepPrice());
        if (req.getBidAmount().compareTo(minimumBid) < 0) {
          conn.rollback();
          return new PlaceBidResponseDTO(false, "Bid không hợp lệ");
        }

        // --- Bid thủ công ---
        transferHighestBidDeposit(conn, req.getAuctionId(),
            auction.getHighestBidderId(), auction.getCurrentHighestPrice(),
            req.getBidderId(), req.getBidAmount());

        if (!bidRepo.saveBid(conn, new BidTransaction(req.getAuctionId(), req.getBidderId(), req.getBidAmount()))) {
          conn.rollback();
          return new PlaceBidResponseDTO(false, "Không thể lưu bid");
        }
        auctionRepo.updatePrice(conn, req.getAuctionId(), req.getBidderId(), req.getBidAmount());
        auction.setCurrentHighestPrice(req.getBidAmount());
        auction.setHighestBidderId(req.getBidderId());

        events.addManualBid(req.getAuctionId(), req.getBidderId(), req.getBidderName(), req.getBidAmount());
        applyAntiSniping(conn, req.getAuctionId(), auction, events);

        // --- Bot phản đòn (cùng transaction) ---
        processBotCounterAttack(conn, req.getAuctionId(), auction, events);

        conn.commit();
        events.dispatch();
        return new PlaceBidResponseDTO(true, "Bid thành công");
      } catch (Exception e) {
        conn.rollback();
        log.error("Lỗi xảy ra trong placeBidWithinLock cho đấu giá {}", req.getAuctionId(), e);
        return new PlaceBidResponseDTO(false, e.getMessage());
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.error("Lỗi kết nối cơ sở dữ liệu khi đặt giá", e);
      return new PlaceBidResponseDTO(false, "Lỗi kết nối cơ sở dữ liệu");
    }
  }

  /**
   * Khi ≥ 2 auto-bid cùng phiên: chốt giá proxy (M2 + step), xử lý ví, thử bot kế nếu top 1 thiếu tiền.
   */
  public void resolveAutoBidFight(String auctionId) {
    PostCommitEvents events = new PostCommitEvents();

    runWithAuctionLock(auctionId, () -> {
      try (Connection conn = DatabaseConnection.getConnection()) {
        conn.setAutoCommit(false);
        try {
          AuctionResponseDTO auction = auctionRepo.findAuctionForUpdate(conn, auctionId);
          if (auction == null || auction.getStatus() != AuctionStatus.ACTIVE) {
            conn.rollback();
            return;
          }

          List<AutoBidConfig> activeBots = new ArrayList<>(autoBidRepo.findActiveBotsOrderedByMaxPrice(conn, auctionId));
          List<String> botsToDeactivate = new ArrayList<>();

          AutoBidConfig finalWinner = null;
          BigDecimal finalPrice = auction.getCurrentHighestPrice();
          String finalBidderId = auction.getHighestBidderId();
          boolean stateChanged = false;

          // --- VÒNG LẶP STATE-MACHINE TRÊN RAM ---
          while (!activeBots.isEmpty()) {
            AutoBidConfig currentTop = activeBots.get(0);

            BigDecimal actualStep = (currentTop.getStepAmount() != null && currentTop.getStepAmount().compareTo(auction.getMinStepPrice()) > 0)
                ? currentTop.getStepAmount()
                : auction.getMinStepPrice();

            // THỨ 1: Bot đang xét ĐÃ LÀ người giữ đỉnh phòng
            if (currentTop.getUserId().equals(finalBidderId)) {
              if (activeBots.size() >= 2) {
                AutoBidConfig challenger = activeBots.get(1);

                // FIX TRÙNG MAX: Nếu giá hiện tại đã bằng hoặc vượt qua giá max của kẻ thách thức -> Kẻ thách thức loại out
                if (finalPrice.compareTo(challenger.getMaxPrice()) >= 0) {
                  botsToDeactivate.add(challenger.getUserId());
                  events.addAutoDefeated(challenger.getUserId(), new AutoBidDefeatedDTO(auctionId, "Bị Bot khác đè bẹp ngân sách!"));
                  activeBots.remove(1);
                  stateChanged = true;
                  continue;
                }

                // Tính giá đè bẹp challenger
                BigDecimal priceToBeat = challenger.getMaxPrice().add(actualStep);
                BigDecimal newPrice = priceToBeat.min(currentTop.getMaxPrice());

                if (newPrice.compareTo(finalPrice) > 0) {
                  BigDecimal requiredFreeze = newPrice.multiply(FREEZE_RATE);
                  BigDecimal previousDeposit = finalPrice.multiply(FREEZE_RATE);
                  Wallet wallet = walletRepo.getWalletByUserIdForUpdate(conn, currentTop.getUserId());

                  BigDecimal effectiveBalance = (wallet != null) ? wallet.getBalance().add(previousDeposit) : BigDecimal.ZERO;

                  if (effectiveBalance.compareTo(requiredFreeze) >= 0) {
                    finalPrice = newPrice;
                    finalWinner = currentTop;
                    stateChanged = true;

                    // FIX TRÙNG MAX: Nếu mức giá mới đã chạm tới hoặc vượt Max của challenger -> Kick challenger ngay
                    if (finalPrice.compareTo(challenger.getMaxPrice()) >= 0) {
                      botsToDeactivate.add(challenger.getUserId());
                      events.addAutoDefeated(challenger.getUserId(), new AutoBidDefeatedDTO(auctionId, "Bị Bot khác đè bẹp ngân sách hoặc trùng mức giá Max!"));
                      activeBots.remove(1);
                    }
                  } else {
                    botsToDeactivate.add(currentTop.getUserId());
                    events.addAutoDefeated(currentTop.getUserId(), new AutoBidDefeatedDTO(auctionId, "Bot tự tắt do thiếu số dư cọc 10%."));
                    activeBots.remove(0);
                    stateChanged = true;
                  }
                } else {
                  // FIX TRÙNG MAX: Nếu không thể tăng giá nữa (do bị chạm trần Max của chính mình) nhưng giá hiện tại bằng Max challenger -> Loại challenger vì mình là người đến trước và giữ đỉnh
                  botsToDeactivate.add(challenger.getUserId());
                  activeBots.remove(1);
                  stateChanged = true;
                }
              } else {
                break;
              }
            }
            // THỨ 2: Bot đang xét CHƯA PHẢI người giữ đỉnh
            else {
              BigDecimal minPriceToBeat = finalPrice.add(actualStep);

              // Ngân sách Bot không đủ để TRẢ CAO HƠN giá hiện tại + step -> Khai tử luôn
              if (currentTop.getMaxPrice().compareTo(minPriceToBeat) < 0) {
                botsToDeactivate.add(currentTop.getUserId());
                events.addAutoDefeated(currentTop.getUserId(), new AutoBidDefeatedDTO(auctionId, "Ngân sách Bot không đủ để đè giá hiện tại!"));
                activeBots.remove(0);
                stateChanged = true;
                continue;
              }

              BigDecimal targetPrice = minPriceToBeat;
              if (activeBots.size() >= 2) {
                AutoBidConfig challenger = activeBots.get(1);
                if (challenger.getMaxPrice().compareTo(finalPrice) >= 0) {
                  BigDecimal priceToBeatChallenger = challenger.getMaxPrice().add(actualStep);
                  targetPrice = priceToBeatChallenger.min(currentTop.getMaxPrice());
                  if (targetPrice.compareTo(minPriceToBeat) < 0) {
                    targetPrice = minPriceToBeat;
                  }
                }
              }

              BigDecimal requiredFreeze = targetPrice.multiply(FREEZE_RATE);
              Wallet wallet = walletRepo.getWalletByUserIdForUpdate(conn, currentTop.getUserId());

              if (wallet != null && wallet.getBalance().compareTo(requiredFreeze) >= 0) {
                finalPrice = targetPrice;
                finalWinner = currentTop;
                finalBidderId = currentTop.getUserId();
                stateChanged = true;

                if (activeBots.size() >= 2) {
                  AutoBidConfig challenger = activeBots.get(1);
                  // FIX TRÙNG MAX: Đổi từ <= sang <= để loại bỏ triệt để challenger bằng tiền
                  if (challenger.getMaxPrice().compareTo(finalPrice) <= 0) {
                    botsToDeactivate.add(challenger.getUserId());
                    events.addAutoDefeated(challenger.getUserId(), new AutoBidDefeatedDTO(auctionId, "Bị Bot của tài phiệt khác đè bẹp ngân sách!"));
                    activeBots.remove(1);
                  }
                }
              } else {
                botsToDeactivate.add(currentTop.getUserId());
                events.addAutoDefeated(currentTop.getUserId(), new AutoBidDefeatedDTO(auctionId, "Bot tự tắt do thiếu số dư cọc 10%."));
                activeBots.remove(0);
                stateChanged = true;
              }
            }
          }

          // --- BƯỚC CHỐT HẠ ---
          if (stateChanged) {
            for (String botUserId : botsToDeactivate) {
              autoBidRepo.deactivate(conn, botUserId, auctionId);
            }

            if (finalWinner != null && finalPrice.compareTo(auction.getCurrentHighestPrice()) > 0) {
              transferHighestBidDeposit(conn, auctionId, auction.getHighestBidderId(), auction.getCurrentHighestPrice(), finalWinner.getUserId(), finalPrice);
              bidRepo.saveBid(conn, new BidTransaction(auctionId, finalWinner.getUserId(), finalPrice));
              auctionRepo.updatePrice(conn, auctionId, finalWinner.getUserId(), finalPrice);

              auction.setCurrentHighestPrice(finalPrice);
              auction.setHighestBidderId(finalWinner.getUserId());
              applyAntiSniping(conn, auctionId, auction, events);

              String winnerName = userRepo.getAccountNameByUserId(finalWinner.getUserId());
              events.addNewBid(new NewBidDTO(auctionId, finalWinner.getUserId(), winnerName, finalPrice));
              events.addPriceUpdate(new AuctionPriceUpdateDTO(auctionId, finalPrice));
            }

            conn.commit();
            events.dispatch();
          } else {
            conn.rollback();
          }

        } catch (Exception e) {
          conn.rollback();
          log.error("Lỗi nghiêm trọng trong resolveAutoBidFight cho đấu giá {}", auctionId, e);
        } finally {
          conn.setAutoCommit(true);
        }
      } catch (SQLException e) {
        log.error("Lỗi kết nối cơ sở dữ liệu trong resolveAutoBidFight", e);
      }
    });
  }

  private void processBotCounterAttack(
      Connection conn,
      String auctionId,
      AuctionResponseDTO auction,
      PostCommitEvents events) throws SQLException {

    List<AutoBidConfig> activeBots = autoBidRepo.findActiveBotsOrderedByMaxPrice(conn, auctionId);
    if (activeBots.isEmpty() || activeBots.get(0).getUserId().equals(auction.getHighestBidderId())) {
      return;
    }

    AutoBidConfig bot = activeBots.get(0);
    BigDecimal currentPrice = auction.getCurrentHighestPrice();
    // 1. Xác định bước giá thực tế của Bot (áp dụng luật sàn nếu Bot cài quá nhỏ hoặc null)
    BigDecimal actualBotStep = (bot.getStepAmount() != null && bot.getStepAmount().compareTo(auction.getMinStepPrice()) > 0)
        ? bot.getStepAmount()
        : auction.getMinStepPrice();

    // 2. Giá Bot cần đặt sẽ bằng Giá hiện tại + Bước giá chiến thuật của Bot
    BigDecimal priceToBeat = currentPrice.add(actualBotStep);

    if (bot.getMaxPrice().compareTo(priceToBeat) >= 0) {
      BigDecimal newBotPrice = priceToBeat;
      BigDecimal requiredFreeze = newBotPrice.multiply(FREEZE_RATE);
      Wallet botWallet = walletRepo.getWalletByUserIdForUpdate(conn, bot.getUserId());

      if (botWallet != null && botWallet.getBalance().compareTo(requiredFreeze) >= 0) {
        transferHighestBidDeposit(conn, auctionId,
            auction.getHighestBidderId(), currentPrice, bot.getUserId(), newBotPrice);
        bidRepo.saveBid(conn, new BidTransaction(auctionId, bot.getUserId(), newBotPrice));
        auctionRepo.updatePrice(conn, auctionId, bot.getUserId(), newBotPrice);
        auction.setCurrentHighestPrice(newBotPrice);
        auction.setHighestBidderId(bot.getUserId());

        applyAntiSniping(conn, auctionId, auction, events);

        String botName = userRepo.getAccountNameByUserId(bot.getUserId());
        events.addNewBid(new NewBidDTO(auctionId, bot.getUserId(), "[Auto] " + botName, newBotPrice));
        events.addPriceUpdate(new AuctionPriceUpdateDTO(auctionId, newBotPrice));
      } else {
        autoBidRepo.deactivate(conn, bot.getUserId(), auctionId);
        String fomoMessage = "Bot đã tự tắt do số dư ví không đủ 10% (" + requiredFreeze
            + " VNĐ) để tiếp tục đấu giá!";
        events.addAutoDefeated(bot.getUserId(), new AutoBidDefeatedDTO(auctionId, fomoMessage));
      }
    } else {
      autoBidRepo.deactivate(conn, bot.getUserId(), auctionId);
      String fomoMessage = "Một tài phiệt khác vừa trả giá đè bẹp ngân sách Bot của bạn!";
      events.addAutoDefeated(bot.getUserId(), new AutoBidDefeatedDTO(auctionId, fomoMessage));
    }
  }

  /**
   * Chuyển cọc 10% từ người giữ top cũ sang người giữ top mới.
   * Luôn release cọc cũ (kể cả khi cùng user) rồi freeze cọc mới — tránh đóng băng trùng.
   */
  private void transferHighestBidDeposit(
      Connection conn,
      String auctionId,
      String previousHighestBidderId,
      BigDecimal previousPrice,
      String newBidderId,
      BigDecimal newPrice) throws SQLException {

    if (previousHighestBidderId != null && !previousHighestBidderId.isEmpty()) {
      BigDecimal releaseAmount = previousPrice.multiply(FREEZE_RATE);
      log.debug("[TRANSFER] Release {} từ {} | previousPrice={}", releaseAmount, previousHighestBidderId, previousPrice);

      // Kiểm tra frozen balance trước khi release
      Wallet prevWallet = walletRepo.getWalletByUserIdForUpdate(conn, previousHighestBidderId);
      log.debug("[TRANSFER] frozenBalance hiện tại của {} = {}", previousHighestBidderId, 
          (prevWallet != null ? prevWallet.getFrozenBalance() : "null"));

      walletService.releaseFrozen(conn, previousHighestBidderId, releaseAmount, auctionId);
    }
    walletService.freezeMoney(conn, newBidderId, newPrice.multiply(FREEZE_RATE), auctionId);
  }

  private void applyAntiSniping(
      Connection conn,
      String auctionId,
      AuctionResponseDTO auction,
      PostCommitEvents events) throws SQLException {

    LocalDateTime endTime = auction.getEndTime();
    if (endTime == null) {
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    long secondsLeft = ChronoUnit.SECONDS.between(now, endTime);
    long thresholdSeconds = AuctionConfig.ANTI_SNIPE_THRESHOLD_MINUTES * 60L;

    // 1. Kiểm tra chính xác từng giây: Nếu chưa vào vùng "đèn đỏ" -> Thoát luôn
    if (secondsLeft >= thresholdSeconds) {
      return;
    }

    // 2. SỬA TẠI ĐÂY: Lấy endTime cũ CỘNG THÊM thời gian (Bơm thêm thời gian đúng ý bạn)
    LocalDateTime newEndTime = endTime.plusMinutes(AuctionConfig.ANTI_SNIPE_EXTENSION_MINUTES);

    // LƯU Ý: Đoạn check (!newEndTime.isAfter(endTime)) cũ được bỏ đi vì endTime + 3 phút chắc chắn luôn lớn hơn endTime cũ.

    // 3. Chốt hạ dữ liệu đồng bộ
    auctionRepo.updateEndTime(conn, auctionId, newEndTime);

    // QUAN TRỌNG: Cập nhật RAM ngay lập tức để nếu Bot phản đòn gọi lại hàm này sau 1ms,
    // Bot sẽ thấy secondsLeft lúc này đã rất lớn và tự động return ở bước 1, tránh được lỗi cộng dồn!
    auction.setEndTime(newEndTime);

    events.addExtension(new AuctionExtendedDTO(auctionId, newEndTime));
    log.info("[ANTI-SNIPING] Gia hạn phiên {} thêm {} phút. Giờ đóng cửa mới: {}", 
        auctionId, AuctionConfig.ANTI_SNIPE_EXTENSION_MINUTES, newEndTime);
  }

  /** Gom broadcast sau commit để client chỉ thấy trạng thái đã chốt DB. */
  private static final class PostCommitEvents {
    private final List<NewBidDTO> newBids = new ArrayList<>();
    private final List<AuctionPriceUpdateDTO> priceUpdates = new ArrayList<>();
    private final List<AuctionExtendedDTO> extensions = new ArrayList<>();
    private final List<UserMessage> userMessages = new ArrayList<>();

    void addManualBid(String auctionId, String bidderId, String bidderName, BigDecimal amount) {
      newBids.add(new NewBidDTO(auctionId, bidderId, bidderName, amount));
      priceUpdates.add(new AuctionPriceUpdateDTO(auctionId, amount));
    }

    void addNewBid(NewBidDTO dto) {
      newBids.add(dto);
    }

    void addPriceUpdate(AuctionPriceUpdateDTO dto) {
      priceUpdates.add(dto);
    }

    void addExtension(AuctionExtendedDTO dto) {
      extensions.add(dto);
    }

    void addAutoDefeated(String userId, AutoBidDefeatedDTO dto) {
      userMessages.add(new UserMessage(userId, dto));
    }

    void dispatch() {
      for (NewBidDTO dto : newBids) {
        Server.broadcastToAuctionRoom(dto);
      }
      for (AuctionPriceUpdateDTO dto : priceUpdates) {
        Server.broadcastToAll(dto);
      }
      for (AuctionExtendedDTO dto : extensions) {
        Server.broadcastToAuctionRoom(dto);
        Server.broadcastToAll(dto);
      }
      for (UserMessage msg : userMessages) {
        Server.sendToUser(msg.userId(), msg.dto());
      }
    }

    private record UserMessage(String userId, AutoBidDefeatedDTO dto) {}
  }
}
