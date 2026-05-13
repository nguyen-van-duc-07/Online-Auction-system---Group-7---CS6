package service;

import com.auction.shared.model.user.SellerProfile;
import com.auction.shared.request.CheckingSellerProfileRequestDTO;
import com.auction.shared.request.SellerRegisterRequestDTO;
import repository.SellerProfileRepository;

public class SellerService {

  private static final SellerProfileRepository sellerRepo = new SellerProfileRepository();

  public static boolean sellerRegister(SellerRegisterRequestDTO sellerRegisterReq) {
    SellerProfile sellerProfile = new SellerProfile(sellerRegisterReq.getUserId(),
        sellerRegisterReq.getBrandName(),
        sellerRegisterReq.getCitizenIdentityCard(),
        sellerRegisterReq.getLocation(),
        sellerRegisterReq.getBankAccount(),
        sellerRegisterReq.getBankName());

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
}
