package servercontroller;

import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.item.Item;
import com.auction.shared.model.item.ItemDTO;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.model.user.User;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.*;
import com.auction.shared.response.*;

import java.util.List;

import repository.AuctionRepository;
import repository.BidTransactionRepository;
import repository.SellerProfileRepository;
import service.*;

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
          .role(loggedInUser.getRole())
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

  public static UploadItemResponseDTO uploadItem(UploadItemRequestDTO uploadItemReq, String authenticatedUserId) {
    // Kiểm tra xem User đã có hồ sơ người bán chưa
    SellerProfileRepository profileRepo = new SellerProfileRepository();
    String sellerProfileId = profileRepo.findProfileIdByUserId(authenticatedUserId);

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

  public static GetAuctionsBySellerResponseDTO getAuctionsBySeller(GetAuctionsBySellerRequestDTO request, String authenticatedUserId) {
    List<AuctionResponseDTO> list = AuctionService.getAuctionsBySeller(authenticatedUserId);
    return new GetAuctionsBySellerResponseDTO(true, "Tải danh sách thành công", list);
  }

  public static UpdateProfileResponseDTO updateProfile(UpdateProfileRequestDTO updateProfileReq, String authenticatedUserId) {
    //updateProfileReq.setId(authenticatedUserId);
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

  public static AuctionResponseDTO joinRoom(JoinRoomRequestDTO request) {
    String auctionId = request.getSelectedAuctionId();
    Auction auction = AuctionService.getAuctionHistory(auctionId);

    if (auction == null) return null;

    AuctionResponseDTO response = new AuctionResponseDTO();
    response.setId(auction.getId());
    //response.setItem(auction.getItem().getName()); // QUAN TRỌNG: Gửi thông tin sản phẩm về
    response.setCurrentHighestPrice(auction.getCurrentHighestPrice());
    response.setMinStepPrice(auction.getMinStepPrice());
    response.setEndTime(auction.getEndTime());
    response.setStatus(auction.getStatus());
    response.setBidHistory(auction.getBidHistory());
    Item itemEntity = auction.getItem();
    if (itemEntity != null) {
      ItemDTO itemDTO = ItemDTO.builder()
              .id(itemEntity.getId())
              .name(itemEntity.getName())
              .description(itemEntity.getDescription())
              .type(itemEntity.getType())
              .CreatedAt(itemEntity.getCreatedAt())
              .build();

      response.setItem(itemDTO); // Bây giờ kiểu dữ liệu đã khớp (ItemDTO)
    }
    // QUAN TRỌNG: Lấy tên người cao nhất thật sự từ Auction object
    // (Đảm bảo trong AuctionService bạn đã JOIN bảng để lấy tên này)
    response.setHighestBidderName(auction.getHighestBidderName());

    return response;
  }

  public static SellerRegisterResponseDTO sellerRegister(SellerRegisterRequestDTO sellerRegisterReq) {
    boolean isSuccess = SellerService.sellerRegister(sellerRegisterReq);
    String message = isSuccess ? "Hồ sơ của bạn đã được tiếp nhận!" : "Không thể đăng kí hồ sơ người bán!";
    return new SellerRegisterResponseDTO(isSuccess, message);
  }

  public static CheckingSellerProfileResponseDTO checkingSellerProfile(CheckingSellerProfileRequestDTO req) {
    boolean isSellerProfileCreated = SellerService.isSellerProfileCreated(req);
    /* Kiểm tra xem đã có hồ sơ người bán chưa, nếu rồi thì kiểm tra xem trạng thái
    hiện tại của hồ sơ người bán là gì và trả về, nếu chưa thì trả về dòng message tương ứng
     */
    String message = isSellerProfileCreated ? SellerService.sellerProfileStatus(req.getUserId()) :
        "Bạn chưa có hồ sơ bán hàng!";
    return new CheckingSellerProfileResponseDTO(isSellerProfileCreated, message);
  }
  public static OrderActionResponseDTO confirmOrder(ConfirmOrderRequestDTO req) {
    OrderService orderService = new OrderService();
    boolean success = orderService.confirmOrder(req.getOrderId());
    if (success) {
      return new OrderActionResponseDTO(true, "Xác nhận thanh toán thành công!");
    }
    return new OrderActionResponseDTO(false, "Xác nhận thanh toán thất bại!");
  }

  public static OrderActionResponseDTO cancelOrder(CancelOrderRequestDTO req) {
    OrderService orderService = new OrderService();
    boolean success = orderService.cancelOrder(req.getOrderId());
    if (success) {
      return new OrderActionResponseDTO(true, "Hủy đơn hàng thành công!");
    }
    return new OrderActionResponseDTO(false, "Hủy đơn hàng thất bại!");
  }
  public static GetOrderResponseDTO getOrder(GetOrderRequestDTO req) {
    OrderService orderService = new OrderService();
    Order order = orderService.getOrderById(req.getOrderId());
    if (order != null) {
      return new GetOrderResponseDTO(true, "Lấy thông tin đơn hàng thành công", order);
    }
    return new GetOrderResponseDTO(false, "Không tìm thấy đơn hàng", null);
  }

  public static GetSellerProfileResponseDTO getSellerProfile(GetSellerProfileRequestDTO request) {
    List<SellerRegisterRequestDTO> list = SellerService.getSellerProfiles();
    GetSellerProfileResponseDTO response = new GetSellerProfileResponseDTO();
    response.setSuccess(true);
    response.setMessage("Tải thông tin thành công");
    response.setSellerProfileList(list);
    return response;
  }

  public static UpdateSellerProfileStatusResponseDTO updateSellerProfileStatus(
      UpdateSellerProfileStatusRequestDTO request) {
    boolean success = SellerService.handleUpdateSellerProfileStatus(request);
    String message = success ? "Cập nhật trạng thái thành công!" : "Lỗi khi cập nhật dữ liệu!";
    UpdateSellerProfileStatusResponseDTO response = new UpdateSellerProfileStatusResponseDTO();
    response.setSuccess(success);
    response.setMessage(message);
    return response;
  }
  private static final AutoBidService autoBidService = new AutoBidService();

  public static AutoBidResponseDTO setAutoBid(SetAutoBidRequestDTO req) {
    boolean success = autoBidService.setAutoBid(req);
    return success
        ? new AutoBidResponseDTO(true, "Đã bật tự động đấu giá!")
        : new AutoBidResponseDTO(false, "Không thể cài đặt tự động đấu giá. Kiểm tra lại giá trị!");
  }

  public static AutoBidResponseDTO cancelAutoBid(CancelAutoBidRequestDTO req) {
    boolean success = autoBidService.cancelAutoBid(req);
    return success
        ? new AutoBidResponseDTO(true, "Đã tắt tự động đấu giá!")
        : new AutoBidResponseDTO(false, "Không thể tắt tự động đấu giá!");
  }
}