package service;

import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.item.Item;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.request.GetAuctionsBySellerRequestDTO;
import com.auction.shared.request.UploadItemRequestDTO;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.request.UpdateAuctionStatusRequestDTO;
import com.auction.shared.response.UpdateAuctionStatusResponseDTO;
import com.auction.shared.response.AuctionStatusUpdateDTO;
import servercontroller.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import repository.AuctionRepository;
import repository.BidTransactionRepository;

/**
 * Lớp AuctionService xử lý logic nghiệp vụ của hệ thống đấu giá.
 * Bao gồm các chức năng như tạo phiên đấu giá, xử lý đặt giá, và quản lý trạng thái đấu giá.
 */
public class AuctionService {
  private static final Logger log = LoggerFactory.getLogger(AuctionService.class);

  private final AuctionRepository auctionRepo;
  private final BidTransactionRepository bidRepo;

  /**
   * Constructor mặc định cho Production.
   */
  public AuctionService() {
    this(AuctionRepository.getInstance(), BidTransactionRepository.getInstance());
  }

  /**
   * Constructor nhận tham số phục vụ cho Unit Test.
   */
  public AuctionService(AuctionRepository auctionRepo, BidTransactionRepository bidRepo) {
    this.auctionRepo = auctionRepo;
    this.bidRepo = bidRepo;
  }

  private static class Holder {
    private static final AuctionService INSTANCE = new AuctionService();
  }

  public static AuctionService getInstance() {
    return Holder.INSTANCE;
  }

  private final Map<String, Auction> auctions = new ConcurrentHashMap<>();

  /**
   * Thực hiện luồng đăng bán sản phẩm mới.
   *
   * <p>Tạo đối tượng Item, lưu vào DB. Nếu thành công, tiếp tục tạo Auction và lưu vào DB.</p>
   *
   * @param request Gói tin DTO chứa thông tin cấu hình từ Client
   * @return {@code true} nếu cả Item và Auction đều được lưu thành công, ngược lại là {@code false}
   */
  public boolean uploadNewAuction(UploadItemRequestDTO request) {
    Item item = com.auction.shared.model.item.ItemFactory.createItem(
        request.getItemName(),
        request.getItemType(),
        request.getDescription(),
        request.getAdditionalAttributes()
    );

    // Lưu ảnh sản phẩm lên đĩa cứng (nếu có)
    String imagePath = null;
    if (request.getImageBytes() != null && request.getImageBytes().length > 0) {
      try {
        imagePath = ImageStorageService.saveImage(request.getImageBytes(), request.getImageExtension());
      } catch (Exception e) {
        log.error("Lỗi lưu ảnh, tiếp tục tạo auction không có ảnh: {}", e.getMessage(), e);
      }
    }

    return createAuction(item,
        request.getUserId(),
        request.getStartPrice(),
        request.getMinStepPrice(),
        request.getStartTime(),
        request.getEndTime(),
        imagePath);
  }

  /**
   * Tạo một phiên đấu giá mới cho một sản phẩm.
   *
   * @param item       sản phẩm được đưa vào đấu giá
   * @param startPrice giá khởi điểm của phiên đấu giá
   * @param startTime  thời gian bắt đầu đấu giá
   * @param endTime    thời gian kết thúc đấu giá
   * @return đối tượng Auction vừa được tạo
   */
  public boolean createAuction(Item item,
                               String userId,
                               BigDecimal startPrice,
                               BigDecimal minStepPrice,
                               LocalDateTime startTime,
                               LocalDateTime endTime,
                               String imagePath) {

    Auction auction = new Auction(item, userId, startPrice, minStepPrice, startTime, endTime);

    boolean isAuctionSaved = auctionRepo.saveAuction(auction, imagePath);

    if (isAuctionSaved) {
      auctions.put(auction.getId(), auction);
      if (auction.getStatus() == AuctionStatus.ACTIVE) {
        NotificationService.getInstance().sendNewAuctionNotification(
            auction.getId(),
            auction.getItem().getName(),
            auction.getStartPrice()
        );
      }
      return true;
    } else {
      return false;
    }
  }

  public AuctionResponseDTO findAuctionById(String id) {
    return auctionRepo.findAuctionById(id);
  }

  /**
   * Bắt đầu một phiên đấu giá nếu hợp lệ.
   *
   * <p>Phiên đấu giá chỉ được bắt đầu khi:
   * - Tồn tại auction theo auctionId
   * - Trạng thái hiện tại là WAITING</p>
   *
   * @param auctionId mã định danh của phiên đấu giá cần bắt đầu
   */
  public void startAuction(String auctionId) {
    Auction auction = auctions.get(auctionId);
    if (auction == null) {
      return;
    }

    auction.start();
  }

  /**
   * Hủy một phiên đấu giá theo mã định danh.
   *
   * <p>Nếu phiên đấu giá tồn tại, hệ thống sẽ chuyển trạng thái
   * của auction sang CANCELLED.</p>
   *
   * @param auctionId mã định danh của phiên đấu giá cần hủy
   */
  public void cancelAuction(String auctionId) {
    Auction auction = auctions.get(auctionId);
    if (auction == null) {
      return;
    }

    auction.cancel();
  }

  /**
   * Lấy thông tin phiên đấu giá theo mã định danh.
   *
   * @param auctionId mã định danh của phiên đấu giá
   * @return đối tượng Auction tương ứng, hoặc null nếu không tồn tại
   */
  public Auction getAuction(String auctionId) {
    return auctions.get(auctionId);
  }

  /**
   * Truy xuất danh sách các phiên đấu giá đang mở (ACTIVE) từ cơ sở dữ liệu
   * và chuyển đổi (mapping) chúng sang định dạng DTO cho Client.
   *
   * @return Danh sách các đối tượng {@link AuctionResponseDTO} chứa thông tin tóm tắt
   * của các sản phẩm đang được đấu giá trên sàn.
   */
  public List<AuctionDTO> getAuctionsForClient(AuctionStatus status) {
    return auctionRepo.findAuctionsByStatusForBidder(status);
  }

  /**
   * Truy xuất danh sách các phiên đấu giá đang mở (ACTIVE) và sắp diễn ra (WAITING)
   * từ cơ sở dữ liệu và chuyển đổi (mapping) chúng sang định dạng DTO cho Client.
   *
   * @return Danh sách các đối tượng {@link AuctionDTO} chứa thông tin tóm tắt
   * của các sản phẩm đang được đấu giá trên sàn.
   */
  public List<AuctionDTO> getActiveAndWaitingAuctions() {
    return auctionRepo.findActiveAndWaitingAuctions();
  }

  public List<AuctionDTO> getAuctionsForSeller(GetAuctionsBySellerRequestDTO req) {
    return auctionRepo.findAuctionsByUserId(req.getUserId());
  }


  public boolean cancelActiveAndWaitingAuctionsBySellerUserId(String userId) {
    List<String> canceledAuctionIds = auctionRepo.findActiveAndWaitingAuctionIdsByUserId(userId);
    boolean success = auctionRepo.cancelActiveAndWaitingAuctionsByUserId(userId);
    if (success && canceledAuctionIds != null) {
      for (String auctionId : canceledAuctionIds) {
        Auction ramAuction = getAuction(auctionId);
        if (ramAuction != null) {
          ramAuction.setStatus(AuctionStatus.CANCELED);
        }
        Server.broadcastToAll(new AuctionStatusUpdateDTO(auctionId, AuctionStatus.CANCELED, true));
      }
    }
    return success;
  }

  public boolean restoreCanceledAuctionsBySellerUserId(String userId) {
    List<String> canceledAuctionIds = auctionRepo.findCanceledAuctionIdsByUserId(userId);
    boolean success = auctionRepo.restoreCanceledAuctionsByUserId(userId, LocalDateTime.now());
    if (success && canceledAuctionIds != null) {
      for (String auctionId : canceledAuctionIds) {
        AuctionResponseDTO updatedAuction = auctionRepo.findAuctionById(auctionId);
        if (updatedAuction != null) {
          Auction ramAuction = getAuction(auctionId);
          if (ramAuction != null) {
            ramAuction.setStatus(updatedAuction.getStatus());
            ramAuction.setStartTime(updatedAuction.getStartTime());
            ramAuction.setEndTime(updatedAuction.getEndTime());
            ramAuction.setCurrentHighestPrice(updatedAuction.getStartPrice());
            ramAuction.setHighestBidderId(null);
          }
          Server.broadcastToAll(new AuctionStatusUpdateDTO(auctionId, updatedAuction.getStatus()));
        }
      }
    }
    return success;
  }

  public AuctionResponseDTO getAuctionHistory(String auctionId) {
    AuctionResponseDTO auction = auctionRepo.findAuctionResponseDTOById(auctionId);

    if (auction != null) {
      List<BidTransaction> history = bidRepo.findRecentByAuctionId(auctionId, 20);
      auction.setBidHistory(history);
    }

    return auction;
  }

  public UpdateAuctionStatusResponseDTO updateAuctionStatusByAdmin(UpdateAuctionStatusRequestDTO request) {
    String auctionId = request.getAuctionId();
    AuctionStatus targetStatus = request.getStatus();

    final UpdateAuctionStatusResponseDTO[] response = new UpdateAuctionStatusResponseDTO[1];
    service.BidService.runWithAuctionLock(auctionId, () -> {
      try {
        AuctionResponseDTO auction = auctionRepo.findAuctionById(auctionId);
        if (auction == null) {
          response[0] = new UpdateAuctionStatusResponseDTO(false, "Phiên đấu giá không tồn tại!");
          return;
        }

        if (targetStatus == AuctionStatus.ACTIVE) {
          if (auction.getStatus() == AuctionStatus.ACTIVE) {
            response[0] = new UpdateAuctionStatusResponseDTO(false, "Phiên hiện đang mở sẵn!");
            return;
          }
          if (auction.getStatus() != AuctionStatus.WAITING && auction.getStatus() != AuctionStatus.CANCELED) {
            response[0] = new UpdateAuctionStatusResponseDTO(false, "Chỉ có thể mở phiên ở trạng thái chờ (WAITING) hoặc đã hủy (CANCELED)!");
            return;
          }

          if (auction.getStatus() == AuctionStatus.CANCELED) {
            boolean success = auctionRepo.restoreSingleCanceledAuction(auctionId, LocalDateTime.now());
            if (success) {
              AuctionResponseDTO updatedAuction = auctionRepo.findAuctionById(auctionId);
              if (updatedAuction != null) {
                Auction ramAuction = getAuction(auctionId);
                if (ramAuction != null) {
                  ramAuction.setStatus(updatedAuction.getStatus());
                  ramAuction.setStartTime(updatedAuction.getStartTime());
                  ramAuction.setEndTime(updatedAuction.getEndTime());
                  ramAuction.setCurrentHighestPrice(updatedAuction.getStartPrice());
                  ramAuction.setHighestBidderId(null);
                }
                Server.broadcastToAll(new AuctionStatusUpdateDTO(auctionId, updatedAuction.getStatus()));
                if (updatedAuction.getStatus() == AuctionStatus.ACTIVE) {
                  NotificationService.getInstance().sendNewAuctionNotification(
                      auctionId,
                      auction.getItem().getName(),
                      auction.getStartPrice()
                  );
                }
              }
              response[0] = new UpdateAuctionStatusResponseDTO(true, "Khôi phục và mở phiên đấu giá thành công!");
            } else {
              response[0] = new UpdateAuctionStatusResponseDTO(false, "Khôi phục phiên đấu giá thất bại!");
            }
          } else {
            // WAITING
            boolean success = auctionRepo.updateAuctionStatusAndStartTime(auctionId, AuctionStatus.ACTIVE.name(), LocalDateTime.now());
            if (success) {
              Auction ramAuction = getAuction(auctionId);
              if (ramAuction != null) {
                ramAuction.setStatus(AuctionStatus.ACTIVE);
                ramAuction.setStartTime(LocalDateTime.now());
              }
              Server.broadcastToAll(new AuctionStatusUpdateDTO(auctionId, AuctionStatus.ACTIVE));
              NotificationService.getInstance().sendNewAuctionNotification(
                  auctionId,
                  auction.getItem().getName(),
                  auction.getStartPrice()
              );
              response[0] = new UpdateAuctionStatusResponseDTO(true, "Mở phiên đấu giá thành công!");
            } else {
              response[0] = new UpdateAuctionStatusResponseDTO(false, "Mở phiên đấu giá thất bại!");
            }
          }
        } else if (targetStatus == AuctionStatus.CLOSED) {
          if (auction.getStatus() == AuctionStatus.ACTIVE) {
            boolean success = auctionRepo.updateAuctionEndTime(auctionId, LocalDateTime.now());
            if (success) {
              Auction ramAuction = getAuction(auctionId);
              if (ramAuction != null) {
                ramAuction.setEndTime(LocalDateTime.now());
              }
              Server.broadcastToAll(new AuctionStatusUpdateDTO(auctionId, AuctionStatus.CLOSED, true));
              response[0] = new UpdateAuctionStatusResponseDTO(true, "Đóng phiên thành công! Phiên sẽ xử lý kết quả ngay lập tức.");
            } else {
              response[0] = new UpdateAuctionStatusResponseDTO(false, "Đóng phiên thất bại!");
            }
          } else if (auction.getStatus() == AuctionStatus.WAITING) {
            boolean success = auctionRepo.cancelAuctionAndReleaseDeposit(auctionId);
            if (success) {
              Auction ramAuction = getAuction(auctionId);
              if (ramAuction != null) {
                ramAuction.setStatus(AuctionStatus.CANCELED);
              }
              Server.broadcastToAll(new AuctionStatusUpdateDTO(auctionId, AuctionStatus.CANCELED, true));
              response[0] = new UpdateAuctionStatusResponseDTO(true, "Đóng phiên thành công (Đã chuyển trạng thái sang Hủy do phiên chưa bắt đầu)!");
            } else {
              response[0] = new UpdateAuctionStatusResponseDTO(false, "Đóng phiên thất bại!");
            }
          } else {
            response[0] = new UpdateAuctionStatusResponseDTO(false, "Không thể đóng phiên đấu giá ở trạng thái này!");
          }
        } else if (targetStatus == AuctionStatus.CANCELED) {
          if (auction.getStatus() == AuctionStatus.ACTIVE || auction.getStatus() == AuctionStatus.WAITING) {
            boolean success = auctionRepo.cancelAuctionAndReleaseDeposit(auctionId);
            if (success) {
              Auction ramAuction = getAuction(auctionId);
              if (ramAuction != null) {
                ramAuction.setStatus(AuctionStatus.CANCELED);
              }
              Server.broadcastToAll(new AuctionStatusUpdateDTO(auctionId, AuctionStatus.CANCELED, true));
              response[0] = new UpdateAuctionStatusResponseDTO(true, "Chặn và hủy phiên đấu giá thành công, đã hoàn trả cọc!");
            } else {
              response[0] = new UpdateAuctionStatusResponseDTO(false, "Chặn phiên đấu giá thất bại!");
            }
          } else {
            response[0] = new UpdateAuctionStatusResponseDTO(false, "Không thể chặn phiên đấu giá ở trạng thái này!");
          }
        } else {
          response[0] = new UpdateAuctionStatusResponseDTO(false, "Trạng thái yêu cầu không hợp lệ!");
        }
      } catch (Exception e) {
        log.error("Lỗi khi admin cập nhật trạng thái phiên đấu giá ID: {}", auctionId, e);
        response[0] = new UpdateAuctionStatusResponseDTO(false, "Lỗi hệ thống: " + e.getMessage());
      }
    });

    return response[0];
  }
}
