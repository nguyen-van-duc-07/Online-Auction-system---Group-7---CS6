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

  private static final SellerProfileRepository sellerRepo = new SellerProfileRepository();

  public static boolean sellerRegister(SellerRegisterRequestDTO sellerRegisterReq) {
    SellerProfile sellerProfile = new SellerProfile(sellerRegisterReq.getUserId(),
        sellerRegisterReq.getBrandName(),
        sellerRegisterReq.getCitizenIdentityCard(),
        sellerRegisterReq.getLocation(),
        sellerRegisterReq.getBankAccount(),
        sellerRegisterReq.getBankName(),
        SellerRegisterStatus.UNREGISTERED.toString());

    boolean success = sellerRepo.createSellerProfile(sellerProfile);
    return success;
  }

  public static boolean isSellerProfileCreated(CheckingSellerProfileRequestDTO checkingSellerProfileReq) {
    String result = sellerRepo.findProfileIdByUserId(checkingSellerProfileReq.getUserId());
    if (result == null) {
      return false;
    } else {
      return true;
    }
  }

  public static String sellerProfileStatus(String userId) {
    String result = sellerRepo.getSellerProfileStatus(userId);
    return result;
  }

  public static List<SellerRegisterRequestDTO> getSellerProfiles() {
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

  public static boolean handleUpdateSellerProfileStatus(UpdateSellerProfileStatusRequestDTO request) {
    String userId = request.getUserId();
    SellerRegisterStatus status = request.getNewStatus();
    boolean success = sellerRepo.updateStatus(userId, status);
    if (success) {
      NotificationService notifService = new NotificationService();

      switch (status) {
        case REGISTERED -> notifService.sendFromNotification(
            NotificationTemplate.sellerApproved(userId)
        );
        case DENIED -> notifService.sendFromNotification(
            NotificationTemplate.sellerRejected(userId)
        );
        default -> {}// UNREGISTERED không cần thông báo
      }
    }
    return success;
  }
}
