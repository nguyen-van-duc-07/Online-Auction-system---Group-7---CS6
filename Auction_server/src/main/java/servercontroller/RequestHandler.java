package servercontroller;

import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.model.user.User;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.*;
import com.auction.shared.response.*;

import java.util.List;

import repository.AuctionRepository;
import repository.BidTransactionRepository;
import repository.SellerProfileRepository;
import service.AuctionService;
import service.AuthService;
import service.BidService;

/**
 * Bộ điều hướng trung tâm (Controller) xử lý logic phân nhánh cho các yêu cầu từ Client.
 *
 * <p>Lớp này đóng vai trò cầu nối thiết yếu giữa tầng mạng (Network) và tầng dịch vụ (Service).
 * Nhiệm vụ của nó là nhận các đối tượng Request cụ thể, kích hoạt các phương thức nghiệp vụ
 * trong tầng Service (như {@code AuthService}), và đóng gói kết quả (trạng thái, thông báo)
 * vào các đối tượng {@code ResponseDTO} để trả về cho Client.</p>
 *
 * @see service.AuthService
 * @see com.auction.shared.response.ResponseDTO
 */
public class RequestHandler {
  public static LoginResponseDTO login(LoginRequestDTO loginReq) {
    User loggedInUser = AuthService.login(loginReq);

    if (loggedInUser != null) {
      UserDTO userDTO = UserDTO.builder()
          .id(loggedInUser.getId())
          .realName(loggedInUser.getRealName())
          .accountName(loggedInUser.getAccountName())
          .email(loggedInUser.getEmail())
          .phoneNumber(loggedInUser.getPhoneNumber())
          .dob(loggedInUser.getDob())
          .address(loggedInUser.getAddress())
          .build();

      return new LoginResponseDTO(true, "Đăng nhập thành công!", userDTO);
    } else {
      return new LoginResponseDTO(false, "Sai tài khoản hoặc mật khẩu", null);
    }
  }

  public static SignUpResponseDTO signup(SignUpRequestDTO signUpReq) {
    boolean isSuccess = AuthService.signUp(signUpReq);
    String msg = isSuccess ? "Đăng ký tài khoản thành công!" :
        "Tài khoản đã tồn tại hoặc lỗi hệ thống!";
    return new SignUpResponseDTO(isSuccess, msg);
  }

  public static UploadItemResponseDTO uploadItem(UploadItemRequestDTO uploadItemReq) {
    // Kiểm tra xem User đã có hồ sơ người bán chưa
    SellerProfileRepository profileRepo = new SellerProfileRepository();
    String sellerProfileId = profileRepo.findProfileIdByUserId(uploadItemReq.getSellerId());

    // Nếu chưa có, chặn lại và báo lỗi về Client
    if (sellerProfileId == null) {
      return new UploadItemResponseDTO(false,
          "Bạn cần cập nhật hồ sơ người bán trước khi đăng sản phẩm!");
    }

    // Nếu đã có, truyền chính xác ID của Hồ sơ người bán (sellerProfileId) xuống Service
    boolean isSuccess = AuctionService.uploadNewItem(uploadItemReq, sellerProfileId);

    String msg = isSuccess ? "Sản phẩm đã được đăng lên sàn đấu giá thành công!" :
        "Lỗi hệ thống, không thể lưu sản phẩm!";
    return new UploadItemResponseDTO(isSuccess, msg);
  }

  /**
   * Xử lý yêu cầu lấy danh sách các phiên đấu giá đang hoạt động từ Client.
   *
   * <p>Đóng vai trò điều phối luồng dữ liệu:
   * 1. Nhận tín hiệu kích hoạt từ Client qua đối tượng Request.
   * 2. Gọi đến tầng {@link AuctionService} để thực hiện nghiệp vụ lấy và làm sạch dữ liệu.
   * 3. Đóng gói danh sách thu được vào đối tượng {@link GetActiveAuctionResponseDTO}
   * để tầng Network gửi trả về Client.</p>
   *
   * @param getActiveAuctionReq Gói tin yêu cầu từ Client (hiện tại đóng vai trò như một tín hiệu báo hiệu, không chứa dữ liệu bên trong).
   * @return Đối tượng {@link GetActiveAuctionResponseDTO} mang theo cờ trạng thái thành công,
   * thông báo hệ thống và danh sách sản phẩm.
   */
  public static GetActiveAuctionResponseDTO getActiveAuctions(GetActiveAuctionRequestDTO getActiveAuctionReq) {
    List<AuctionResponseDTO> list = AuctionService.getActiveAuctionsForClient();
    return new GetActiveAuctionResponseDTO(true, "Tải danh sách thành công", list);
  }

  public static UpdateProfileResponseDTO updateProfile(UpdateProfileRequestDTO updateProfileReq) {
    User userAfterUpdatingProfile = AuthService.updateProfile(updateProfileReq);

    if (userAfterUpdatingProfile != null) {
      UserDTO userDTO = UserDTO.builder()
          .id(userAfterUpdatingProfile.getId())
          .realName(userAfterUpdatingProfile.getRealName())
          .accountName(userAfterUpdatingProfile.getAccountName())
          .email(userAfterUpdatingProfile.getEmail())
          .phoneNumber(userAfterUpdatingProfile.getPhoneNumber())
          .dob(userAfterUpdatingProfile.getDob())
          .address(userAfterUpdatingProfile.getAddress())
          .build();
      return new UpdateProfileResponseDTO(true, "Cập nhật thông tin tài khoản thành công", userDTO);
    } else {
      return new UpdateProfileResponseDTO(false, "Không thể cập nhật thông tin tài khoản", null);
    }
  }

  public static PlaceBidResponseDTO placeBid(PlaceBidRequestDTO req) {
    BidService bidService = new BidService();
    return bidService.placeBid(req);
  }

  public static AuctionResponseDTO joinRoom(JoinRoomRequestDTO req) {
    String auctionId = req.getSelectedAuctionId();

    // Gọi Service để lấy thông tin chi tiết phiên đấu giá kèm lịch sử
    // Chúng ta sử dụng AuctionService để đảm bảo tính đóng gói
    Auction auction = AuctionService.getAuctionHistory(auctionId);

    if (auction == null) {
      return null; // Hoặc trả về một thông báo lỗi tùy logic của bạn
    }

    // Chuyển đổi từ Entity sang DTO để trả về Client
    AuctionResponseDTO response = new AuctionResponseDTO();
    response.setId(auction.getId());
    response.setCurrentHighestPrice(auction.getCurrentHighestPrice());
    response.setMinStepPrice(auction.getMinStepPrice());
    response.setEndTime(auction.getEndTime());
    response.setStatus(auction.getStatus());

    // Đính kèm danh sách BidTransaction (Entity) trực tiếp vào DTO
    response.setBidHistory(auction.getBidHistory());

    return response;
  }
}
