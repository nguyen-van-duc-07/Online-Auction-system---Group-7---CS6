package servercontroller;

import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.auction.AutoBidConfig;
import com.auction.shared.model.notification.Notification;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.model.user.User;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.*;
import com.auction.shared.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.List;
import com.auction.shared.model.transaction.WalletTransaction;
import repository.AutoBidConfigRepository;
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
  private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
  public static LoginResponseDTO login(LoginRequestDTO loginReq) {
    User loggedInUser = AuthService.login(loginReq);

    if (loggedInUser != null) {
      UserDTO userDTO = UserDTO.builder()
              .role(loggedInUser.getRole())
              .id(loggedInUser.getId())
              .accountName(loggedInUser.getAccountName())
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

  public static void logout(LogoutRequestDTO logoutReq) {
    String userId = logoutReq.getUserId();
    Server.unregisterClient(userId);
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
    if (!profileRepo.haveSellerProfile(uploadItemReq.getSellerId()))
      return new UploadItemResponseDTO(false,
          "Bạn cần cập nhật hồ sơ người bán trước khi đăng sản phẩm!");
    // Nếu đã có, truyền chính xác ID của Hồ sơ người bán (sellerProfileId) xuống Service
    boolean isSuccess = AuctionService.uploadNewItem(uploadItemReq);
    String msg = isSuccess ? "Sản phẩm đã được đăng lên sàn đấu giá thành công!" :
            "Lỗi hệ thống, không thể lưu sản phẩm!";
    return new UploadItemResponseDTO(isSuccess, msg);
  }

  public static AuctionResponseDTO handleFindAuctionById(AuctionRequestDTO request) {
    String auctionId = request.getAuctionId();
    AuctionResponseDTO auction = AuctionService.findAuctionById(auctionId);
    return auction;
  }

  /**
   * Xử lý yêu cầu lấy danh sách các phiên đấu giá đang hoạt động từ Client.
   *
   * <p>Đóng vai trò điều phối luồng dữ liệu:
   * 1. Nhận tín hiệu kích hoạt từ Client qua đối tượng Request.
   * 2. Gọi đến tầng {@link AuctionService} để thực hiện nghiệp vụ lấy và làm sạch dữ liệu.
   * 3. Đóng gói danh sách thu được vào đối tượng {@link GetActiveAuctionsResponseDTO}
   * để tầng Network gửi trả về Client.</p>
   *
   * @param getActiveAuctionReq Gói tin yêu cầu từ Client (hiện tại đóng vai trò như một tín hiệu báo hiệu, không chứa dữ liệu bên trong).
   * @return Đối tượng {@link GetActiveAuctionsResponseDTO} mang theo cờ trạng thái thành công,
   * thông báo hệ thống và danh sách sản phẩm.
   */
  public static GetActiveAuctionsResponseDTO getActiveAuctions(GetActiveAuctionsRequestDTO getActiveAuctionReq) {
    List<AuctionDTO> list = AuctionService.getActiveAuctionsForClient();
    return new GetActiveAuctionsResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public static GetWaitingAuctionsResponseDTO getWaitingAuctions(GetWaitingAuctionsRequestDTO request) {
    List<AuctionDTO> list = AuctionService.getWaitingAuctionsForClient();
    return new GetWaitingAuctionsResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public static GetClosedAuctionsResponseDTO getClosedAuctions(GetClosedAuctionsRequestDTO request) {
    List<AuctionDTO> list = AuctionService.getClosedAuctionsForClient();
    return new GetClosedAuctionsResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public static GetActiveAndWaitingAuctionsResponseDTO getActiveAndWaitingAuctions(
          GetActiveAndWaitingAuctionsRequestDTO request) {
    List<AuctionDTO> list = AuctionService.getActiveAndWaitingAuctions();
    return new GetActiveAndWaitingAuctionsResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public static GetActiveAuctionsBySellerResponseDTO getActiveAuctionsBySeller(
      GetActiveAuctionsBySellerRequestDTO request) {
    List<AuctionDTO> list = AuctionService.getActiveAuctionsBySeller(request.getUserId());
    return new GetActiveAuctionsBySellerResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public static GetAuctionsBySellerResponseDTO getAuctionsBySeller(GetAuctionsBySellerRequestDTO request) {
    List<AuctionDTO> list = AuctionService.getAuctionsBySeller(request.getUserId());
    return new GetAuctionsBySellerResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public static UpdateProfileResponseDTO updateProfile(UpdateProfileRequestDTO updateProfileReq) {
    User userAfterUpdatingProfile = AuthService.updateProfile(updateProfileReq);

    if (userAfterUpdatingProfile != null) {
      UserDTO userDTO = UserDTO.builder()
              .id(userAfterUpdatingProfile.getId())
              .accountName(userAfterUpdatingProfile.getAccountName())
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

  public static JoinRoomResponseDTO joinRoom(JoinRoomRequestDTO request, String userId) {
    String auctionId = request.getSelectedAuctionId();
    AuctionResponseDTO auction = AuctionService.getAuctionHistory(auctionId);
    JoinRoomResponseDTO response = new JoinRoomResponseDTO();

    if (auction != null) {
      boolean success = true;
      String message = "Tải thông tin thành công!";
      response.setSuccess(success);
      response.setMessage(message);
      response.setAuction(auction);

      if (userId != null) {
        AutoBidConfigRepository autoBidRepo = new AutoBidConfigRepository();
        response.setAutoBidConfig(autoBidRepo.findActiveByUserIdAndAuctionId(userId, auctionId));
      }

      return response;
    } else {
      response.setSuccess(false);
      response.setMessage("Tải thông tin thất bại!");
      return response;
    }
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
    try {
      OrderService orderService = new OrderService();
      boolean success = orderService.confirmOrder(req.getOrderId(), req.getBuyerInfo());
      if (success) {
        return new OrderActionResponseDTO(true, "Xác nhận thanh toán thành công!");
      }
      return new OrderActionResponseDTO(false, "Xác nhận thanh toán thất bại! Đơn hàng không tồn tại.");
    } catch (Exception e) {
      String errorMsg = e.getMessage();
      if (e.getCause() != null) {
        errorMsg = e.getCause().getMessage();
      }
      return new OrderActionResponseDTO(false, "Xác nhận thanh toán thất bại: " + errorMsg);
    }
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
      String itemName = "Sản phẩm";
      String itemId = "";
      try {
        repository.AuctionRepository auctionRepo = new repository.AuctionRepository();
        String fetchedItemId = auctionRepo.getItemIdByAuctionId(order.getAuctionId());
        if (fetchedItemId != null) {
          repository.ItemRepository itemRepo = new repository.ItemRepository();
          com.auction.shared.model.item.ItemDTO itemDTO = itemRepo.findById(fetchedItemId);
          if (itemDTO != null) {
            itemName = itemDTO.getName();
            itemId = itemDTO.getId();
          }
        }
      } catch (Exception e) {
        log.error("Lỗi khi truy vấn thông tin sản phẩm cho đơn hàng: {}", order.getId(), e);
      }
      return new GetOrderResponseDTO(true, "Lấy thông tin đơn hàng thành công", order, itemName, itemId);
    }
    return new GetOrderResponseDTO(false, "Không tìm thấy đơn hàng", null, null, null);
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

  public static CancelSellerAuctionsResponseDTO cancelSellerAuctions(CancelSellerAuctionsRequestDTO request) {
    boolean success = AuctionService.cancelActiveAndWaitingAuctionsBySellerUserId(request.getUserId());
    String message = success
        ? "Đã hủy các phiên đấu giá đang/sắp diễn ra của seller!"
        : "Không thể hủy các phiên đấu giá của seller!";
    return new CancelSellerAuctionsResponseDTO(success, message);
  }

  public static RestoreSellerAuctionsResponseDTO restoreSellerAuctions(RestoreSellerAuctionsRequestDTO request) {
    boolean success = AuctionService.restoreCanceledAuctionsBySellerUserId(request.getUserId());
    String message = success
        ? "Đã mở lại các phiên đấu giá bị hủy của seller!"
        : "Không thể mở lại các phiên đấu giá của seller!";
    return new RestoreSellerAuctionsResponseDTO(success, message);
  }

  private static final AutoBidService autoBidService = new AutoBidService();

  public static AutoBidResponseDTO setAutoBid(SetAutoBidRequestDTO req) {
    // Lưu cấu hình Bot xuống DB như cũ
    boolean success = autoBidService.setAutoBid(req);

    if (success) {
      // --- KÍCH HOẠT THUẬT TOÁN ĐỤNG ĐỘ BOT VS BOT TẠI ĐÂY ---
      if (req.isActive()) {
        new BidService().resolveAutoBidFight(req.getAuctionId());
      }
      return new AutoBidResponseDTO(true, "Đã cập nhật tự động đấu giá!");
    } else {
      return new AutoBidResponseDTO(false, "Không thể cài đặt tự động đấu giá. Kiểm tra lại giá trị!");
    }
  }

  public static AutoBidResponseDTO cancelAutoBid(CancelAutoBidRequestDTO req) {
    boolean success = autoBidService.cancelAutoBid(req);
    return success
            ? new AutoBidResponseDTO(true, "Đã tắt tự động đấu giá!")
            : new AutoBidResponseDTO(false, "Không thể tắt tự động đấu giá!");
  }

  public static GetBalanceResponseDTO getBalance(String userId) {
    WalletService walletService = new WalletService();
    try {
      BigDecimal currentBalance = walletService.getBalance(userId);
      return new GetBalanceResponseDTO(true, "Lấy số dư thành công", currentBalance);

    } catch (Exception e) {
      log.error("Lỗi khi xử lý số dư trong RequestHandler cho user: {}", userId, e);
      return new GetBalanceResponseDTO(false, "Không tìm thấy thông tin ví hoặc lỗi hệ thống", BigDecimal.ZERO);
    }
  }

  public static GetNotificationsResponseDTO getNotifications(GetNotificationsRequestDTO req) {
    NotificationService notifService = new NotificationService();
    List<Notification> notifications = notifService.getNotifications(req.getUserId());
    // Dem so thong bao chua doc ( isRead = false)
    int unreadCount = (int) notifications.stream().filter(n -> !n.isRead()).count();
    return new GetNotificationsResponseDTO(true, notifications, unreadCount);
  }
  public static void markNotificationRead(MarkNotificationReadRequestDTO req) {
    NotificationService notifService = new NotificationService();
    if (req.isMarkAll()) {
      notifService.markAllAsRead(req.getUserId());
    } else {
      notifService.markAsRead(req.getNotificationId());
    }
  }

  public static GetPendingOrdersOfSellerResponseDTO handleGetPendingOrdersOfSeller(
      GetPendingOrdersOfSellerRequestDTO req) {
    OrderService orderService = new OrderService();
    List<OrderDTO> pendingOrders =  orderService.getPendingOrdersBySellerId(req.getSellerId());
    return new GetPendingOrdersOfSellerResponseDTO("Tải danh sách thành công", true, pendingOrders);
  }

  public static GetPendingOrdersOfBuyerResponseDTO handleGetPendingOrdersOfBuyer(
      GetPendingOrdersOfBuyerRequestDTO req) {
    OrderService orderService = new OrderService();
    List<OrderDTO> pendingOrders =  orderService.getPendingOrdersByBuyerId(req.getBuyerId());
    return new GetPendingOrdersOfBuyerResponseDTO("Tải danh sách thành công", true, pendingOrders);
  }

  public static GetCompletedOrdersOfSellerResponseDTO handleGetCompletedOrdersOfSeller(
      GetCompletedOrdersOfSellerRequestDTO req) {
    OrderService orderService = new OrderService();
    List<OrderDTO> completedOrders =  orderService.getCompletedOrdersBySellerId(req.getSellerId());
    return new GetCompletedOrdersOfSellerResponseDTO("Tải danh sách thành công", true, completedOrders);
  }

  public static GetCancelledOrdersOfSellerResponseDTO handleGetCancelledOrdersOfSeller(
      GetCancelledOrdersOfSellerRequestDTO req) {
    OrderService orderService = new OrderService();
    List<OrderDTO> cancelledOrders =  orderService.getCancelledOrdersBySellerId(req.getSellerId());
    return new GetCancelledOrdersOfSellerResponseDTO("Tải danh sách thành công", true, cancelledOrders);
  }

  public static GetCompletedOrdersOfBuyerResponseDTO handleGetCompletedOrdersOfBuyer(
      GetCompletedOrdersOfBuyerRequestDTO req) {
    OrderService orderService = new OrderService();
    List<OrderDTO> completedOrders =  orderService.getCompletedOrdersByBuyerId(req.getBuyerId());
    return new GetCompletedOrdersOfBuyerResponseDTO("Tải danh sách thành công", true, completedOrders);
  }

  public static GetCancelledOrdersOfBuyerResponseDTO handleGetCancelledOrdersOfBuyer(
      GetCancelledOrdersOfBuyerRequestDTO req) {
    OrderService orderService = new OrderService();
    List<OrderDTO> cancelledOrders =  orderService.getCancelledOrdersByBuyerId(req.getBuyerId());
    return new GetCancelledOrdersOfBuyerResponseDTO("Tải danh sách thành công", true, cancelledOrders);
  }

  public static CreateAdminResponseDTO createAdmin(CreateAdminRequestDTO req) {
    boolean isSuccess = AuthService.createAdmin(req);
    String msg = isSuccess ? "Đăng ký tài khoản admin thành công!" :
        "Tài khoản đã tồn tại hoặc lỗi hệ thống!";
    return new CreateAdminResponseDTO(isSuccess, msg);
  }


  public static CreateTransactionResponseDTO createTransactionRequest(CreateTransactionRequestDTO req) {
    WalletService walletService = new WalletService();
    boolean success = walletService.createTransactionRequest(req.getUserId(), req.getAmount(), req.getType());
    String message = success ? "Yêu cầu giao dịch đang được xử lý!" : "Lỗi khi tạo yêu cầu giao dịch!";
    return new CreateTransactionResponseDTO(success, message);
  }

  public static GetPendingTransactionsResponseDTO getPendingTransactions(GetPendingTransactionsRequestDTO req) {
    WalletService walletService = new WalletService();
    List<WalletTransaction> pendingTransactions = walletService.getPendingTransactions();
    boolean success = pendingTransactions != null;
    String message = success ? "Tải danh sách thành công" : "Lỗi tải danh sách!";
    return new GetPendingTransactionsResponseDTO(success, message, pendingTransactions);
  }

  public static ProcessTransactionResponseDTO processTransactionRequest(ProcessTransactionRequestDTO req) {
    WalletService walletService = new WalletService();
    boolean success = walletService.processTransactionRequest(req.getTransactionId(), req.getActionStatus());
    String message = success ? "Duyệt giao dịch thành công!" : "Lỗi khi duyệt giao dịch!";
    return new ProcessTransactionResponseDTO(success, message);
  }
}
