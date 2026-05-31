package service;

import com.auction.shared.enums.NotificationType;
import com.auction.shared.enums.SellerRegisterStatus;
import com.auction.shared.model.user.SellerProfile;
import com.auction.shared.request.CheckingSellerProfileRequestDTO;
import com.auction.shared.request.SellerRegisterRequestDTO;
import com.auction.shared.request.UpdateSellerProfileStatusRequestDTO;
import com.auction.shared.response.ResponseDTO;
import com.auction.shared.response.UpdateSellerProfileStatusResponseDTO;
import com.auction.shared.util.NotificationTemplate;
import repository.SellerProfileRepository;

import java.util.ArrayList;
import java.util.List;

public class SellerService {

  private final SellerProfileRepository sellerRepo;
  private final AuctionService auctionService;

  private static class Holder {
    private static final SellerService INSTANCE = new SellerService();
  }

  public static SellerService getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * Constructor mặc định cho Production.
   */
  private SellerService() {
    this(SellerProfileRepository.getInstance(), AuctionService.getInstance());
  }

  /**
   * Constructor nhận tham số phục vụ cho Unit Test.
   */
  public SellerService(SellerProfileRepository sellerRepo, AuctionService auctionService) {
    this.sellerRepo = sellerRepo;
    this.auctionService = auctionService;
  }

  public boolean sellerRegister(SellerRegisterRequestDTO sellerRegisterReq) {
    SellerProfile sellerProfile = new SellerProfile(sellerRegisterReq.getUserId(),
        sellerRegisterReq.getBrandName(),
        sellerRegisterReq.getCitizenIdentityCard(),
        sellerRegisterReq.getLocation(),
        sellerRegisterReq.getBankAccount(),
        sellerRegisterReq.getBankName(),
        SellerRegisterStatus.UNREGISTERED.toString());

    boolean success = sellerRepo.createSellerProfile(sellerProfile);
    if (success) {
      NotificationService notifService = NotificationService.getInstance();
      notifService.sendFromNotification(NotificationTemplate.sellerSubmitted(sellerRegisterReq.getUserId()));
    }
    return success;
  }

  public boolean isSellerProfileCreated(CheckingSellerProfileRequestDTO checkingSellerProfileReq) {
    String result = sellerRepo.findProfileIdByUserId(checkingSellerProfileReq.getUserId());
    if (result == null) {
      return false;
    } else {
      return true;
    }
  }

  public String sellerProfileStatus(String userId) {
    String result = sellerRepo.getSellerProfileStatus(userId);
    return result;
  }

  public List<SellerRegisterRequestDTO> getSellerProfiles() {
    List<SellerRegisterRequestDTO> sellerProfileResponseDTOS = new ArrayList<>();
    List<SellerProfile> sellerProfiles = sellerRepo.getAllSellerProfiles();
    for (SellerProfile sellerProfile : sellerProfiles) {
      SellerRegisterRequestDTO responseDTO = new SellerRegisterRequestDTO();
      responseDTO.setUserId(sellerProfile.getUserId());
      responseDTO.setBrandName(sellerProfile.getBrandName());
      responseDTO.setCitizenIdentityCard(sellerProfile.getCitizenIdentityCard());
      responseDTO.setLocation(sellerProfile.getLocation());
      responseDTO.setBankAccount(sellerProfile.getBankAccount());
      responseDTO.setBankName(sellerProfile.getBankName());
      responseDTO.setStatus(sellerProfile.getStatus());
      responseDTO.setCreatedAt(sellerProfile.getCreatedAt());

      sellerProfileResponseDTOS.add(responseDTO);
    }

    return sellerProfileResponseDTOS;
  }

  public UpdateSellerProfileStatusResponseDTO handleUpdateSellerProfileStatus(UpdateSellerProfileStatusRequestDTO request) {
    String userId = request.getUserId();
    SellerRegisterStatus status = request.getNewStatus();
    SellerRegisterStatus expectedStatus = request.getExpectedOldStatus();

    // 1. Kiểm tra xung đột trạng thái đồng thời (OCC)
    if (expectedStatus != null) {
      String currentStatusStr = sellerRepo.getSellerProfileStatus(userId);
      if (currentStatusStr == null) {
        return new UpdateSellerProfileStatusResponseDTO(false, "Hồ sơ người bán không tồn tại!");
      }
      SellerRegisterStatus currentStatus = SellerRegisterStatus.valueOf(currentStatusStr);
      if (currentStatus != expectedStatus) {
        return new UpdateSellerProfileStatusResponseDTO(false, 
            "Xung đột dữ liệu: Hồ sơ này đã được cập nhật bởi một Admin khác thành '" + currentStatus + "'. Vui lòng tải lại danh sách!");
      }
    }

    // 2. Tiến hành cập nhật trạng thái trong DB
    boolean success = sellerRepo.updateStatus(userId, status);
    if (success) {
      // 3. Tự động xử lý kích hoạt/hủy đấu giá đồng bộ trên Server
      if (status == SellerRegisterStatus.REGISTERED) {
        auctionService.restoreCanceledAuctionsBySellerUserId(userId);
      } else if (status == SellerRegisterStatus.DENIED) {
        auctionService.cancelActiveAndWaitingAuctionsBySellerUserId(userId);
      }

      // 4. Gửi thông báo cho người dùng
      NotificationService notifService = NotificationService.getInstance();
      switch (status) {
        case REGISTERED -> notifService.sendFromNotification(
            NotificationTemplate.sellerApproved(userId)
        );
        case DENIED -> notifService.sendFromNotification(
            NotificationTemplate.sellerRejected(userId)
        );
        default -> {}
      }
      return new UpdateSellerProfileStatusResponseDTO(true, "Cập nhật trạng thái người bán thành công!");
    } else {
      return new UpdateSellerProfileStatusResponseDTO(false, "Lỗi cập nhật trạng thái vào cơ sở dữ liệu!");
    }
  }
}
