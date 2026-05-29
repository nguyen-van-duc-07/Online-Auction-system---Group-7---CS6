package servercontroller;

import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.notification.Notification;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.model.user.User;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.*;
import com.auction.shared.response.*;
import com.auction.shared.model.transaction.WalletTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.List;
import com.auction.shared.enums.SellerRegisterStatus;
import repository.AutoBidConfigRepository;
import repository.SellerProfileRepository;
import service.*;

/**
 * Lớp RequestDispatcher điều phối xử lý nghiệp vụ thực tế (instance-based).
 * Hỗ trợ Unit Test bằng cách cho phép mock/inject các dependency service.
 */
public class RequestDispatcher {
  private static final Logger log = LoggerFactory.getLogger(RequestDispatcher.class);

  private final AuthService authService;
  private final AuctionService auctionService;
  private final BidService bidService;
  private final SellerService sellerService;
  private final OrderService orderService;
  private final AutoBidService autoBidService;
  private final WalletService walletService;
  private final NotificationService notifService;
  private final SellerProfileRepository sellerProfileRepo;
  private final AutoBidConfigRepository autoBidRepo;

  /**
   * Constructor mặc định cho môi trường chạy thực tế (Production).
   */
  public RequestDispatcher() {
    this(
        new AuthService(),
        AuctionService.getInstance(),
        new BidService(),
        new SellerService(),
        new OrderService(),
        new AutoBidService(),
        new WalletService(),
        new NotificationService(),
        new SellerProfileRepository(),
        new AutoBidConfigRepository()
    );
  }

  /**
   * Constructor nhận đầy đủ các dependency phục vụ cho việc truyền Mockito mock.
   */
  public RequestDispatcher(
      AuthService authService,
      AuctionService auctionService,
      BidService bidService,
      SellerService sellerService,
      OrderService orderService,
      AutoBidService autoBidService,
      WalletService walletService,
      NotificationService notifService,
      SellerProfileRepository sellerProfileRepo,
      AutoBidConfigRepository autoBidRepo
  ) {
    this.authService = authService;
    this.auctionService = auctionService;
    this.bidService = bidService;
    this.sellerService = sellerService;
    this.orderService = orderService;
    this.autoBidService = autoBidService;
    this.walletService = walletService;
    this.notifService = notifService;
    this.sellerProfileRepo = sellerProfileRepo;
    this.autoBidRepo = autoBidRepo;
  }

  public LoginResponseDTO login(LoginRequestDTO loginReq) {
    User loggedInUser = authService.login(loginReq);

    if (loggedInUser != null) {
      UserDTO userDTO = UserDTO.builder()
              .role(loggedInUser.getRole())
              .id(loggedInUser.getId())
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

  public SignUpResponseDTO signup(SignUpRequestDTO signUpReq) {
    boolean isSuccess = authService.signUp(signUpReq);
    String msg = isSuccess ? "Đăng ký tài khoản thành công!" :
            "Tài khoản đã tồn tại hoặc lỗi hệ thống!";
    return new SignUpResponseDTO(isSuccess, msg);
  }

  public UploadItemResponseDTO uploadItem(UploadItemRequestDTO uploadItemReq) {
    String status = sellerProfileRepo.getSellerProfileStatus(uploadItemReq.getUserId());
    if (status == null) {
      return new UploadItemResponseDTO(false,
          "Bạn chưa có hồ sơ bán hàng! Vui lòng đăng ký trước khi đăng sản phẩm.");
    }
    if (!SellerRegisterStatus.REGISTERED.name().equals(status)) {
      return new UploadItemResponseDTO(false,
          "Hồ sơ người bán của bạn chưa được duyệt hoặc đã bị chặn!");
    }
    boolean isSuccess = auctionService.uploadNewAuction(uploadItemReq);
    String msg = isSuccess ? "Sản phẩm đã được đăng lên sàn đấu giá thành công!" :
            "Lỗi hệ thống, không thể lưu sản phẩm!";
    return new UploadItemResponseDTO(isSuccess, msg);
  }

  public AuctionResponseDTO handleFindAuctionById(AuctionRequestDTO request) {
    String auctionId = request.getAuctionId();
    return auctionService.findAuctionById(auctionId);
  }

  public GetActiveAuctionsResponseDTO getActiveAuctions(GetActiveAuctionsRequestDTO getActiveAuctionReq) {
    List<AuctionDTO> list = auctionService.getActiveAuctionsForClient();
    return new GetActiveAuctionsResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public GetWaitingAuctionsResponseDTO getWaitingAuctions(GetWaitingAuctionsRequestDTO request) {
    List<AuctionDTO> list = auctionService.getWaitingAuctionsForClient();
    return new GetWaitingAuctionsResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public GetClosedAuctionsResponseDTO getClosedAuctions(GetClosedAuctionsRequestDTO request) {
    List<AuctionDTO> list = auctionService.getClosedAuctionsForClient();
    return new GetClosedAuctionsResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public GetActiveAndWaitingAuctionsResponseDTO getActiveAndWaitingAuctions(
          GetActiveAndWaitingAuctionsRequestDTO request) {
    List<AuctionDTO> list = auctionService.getActiveAndWaitingAuctions();
    return new GetActiveAndWaitingAuctionsResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public GetCanceledAuctionsResponseDTO getCanceledAuctions(
          GetCanceledAuctionsRequestDTO request) {
    List<AuctionDTO> list = auctionService.getCanceledAuctionsForClient();
    return new GetCanceledAuctionsResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public GetActiveAuctionsBySellerResponseDTO getActiveAuctionsBySeller(
      GetActiveAuctionsBySellerRequestDTO request) {
    List<AuctionDTO> list = auctionService.getActiveAuctionsBySeller(request.getUserId());
    return new GetActiveAuctionsBySellerResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public GetAuctionsBySellerResponseDTO getAuctionsBySeller(GetAuctionsBySellerRequestDTO request) {
    List<AuctionDTO> list = auctionService.getAuctionsBySeller(request.getUserId());
    return new GetAuctionsBySellerResponseDTO(true, "Tải danh sách thành công!", list);
  }

  public UpdateProfileResponseDTO updateProfile(UpdateProfileRequestDTO updateProfileReq) {
    User userAfterUpdatingProfile = authService.updateProfile(updateProfileReq);

    if (userAfterUpdatingProfile != null) {
      UserDTO userDTO = UserDTO.builder()
              .id(userAfterUpdatingProfile.getId())
              .role(userAfterUpdatingProfile.getRole())
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

  public ChangePasswordResponseDTO changePassword(ChangePasswordRequestDTO req) {
    return authService.changePassword(req);
  }

  public PlaceBidResponseDTO placeBid(PlaceBidRequestDTO req) {
    return bidService.placeBid(req);
  }

  public JoinRoomResponseDTO joinRoom(JoinRoomRequestDTO request, String userId) {
    String auctionId = request.getSelectedAuctionId();
    AuctionResponseDTO auction = auctionService.getAuctionHistory(auctionId);
    JoinRoomResponseDTO response = new JoinRoomResponseDTO();

    if (auction != null) {
      boolean success = true;
      String message = "Tải thông tin thành công!";
      response.setSuccess(success);
      response.setMessage(message);
      response.setAuction(auction);

      if (userId != null) {
        response.setAutoBidConfig(autoBidRepo.findActiveByUserIdAndAuctionId(userId, auctionId));
      }

      return response;
    } else {
      response.setSuccess(false);
      response.setMessage("Tải thông tin thất bại!");
      return response;
    }
  }

  public SellerRegisterResponseDTO sellerRegister(SellerRegisterRequestDTO sellerRegisterReq) {
    boolean isSuccess = sellerService.sellerRegister(sellerRegisterReq);
    String message = isSuccess ? "Hồ sơ của bạn đã được tiếp nhận!" : "Không thể đăng kí hồ sơ người bán!";
    return new SellerRegisterResponseDTO(isSuccess, message);
  }

  public CheckingSellerProfileResponseDTO checkingSellerProfile(CheckingSellerProfileRequestDTO req) {
    boolean isSellerProfileCreated = sellerService.isSellerProfileCreated(req);
    String message = isSellerProfileCreated ? sellerService.sellerProfileStatus(req.getUserId()) :
            "Bạn chưa có hồ sơ bán hàng!";
    return new CheckingSellerProfileResponseDTO(isSellerProfileCreated, message);
  }

  public OrderActionResponseDTO confirmOrder(ConfirmOrderRequestDTO req) {
    try {
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

  public OrderActionResponseDTO cancelOrder(CancelOrderRequestDTO req) {
    boolean success = orderService.cancelOrder(req.getOrderId());
    if (success) {
      return new OrderActionResponseDTO(true, "Hủy đơn hàng thành công!");
    }
    return new OrderActionResponseDTO(false, "Hủy đơn hàng thất bại!");
  }

  public GetOrderResponseDTO getOrder(GetOrderRequestDTO req) {
    Order order = orderService.getOrderById(req.getOrderId());
    if (order != null) {
      return new GetOrderResponseDTO(true, "Lấy thông tin đơn hàng thành công", order);
    }
    return new GetOrderResponseDTO(false, "Không tìm thấy đơn hàng", null);
  }

  public GetSellerProfileResponseDTO getSellerProfile(GetSellerProfileRequestDTO request) {
    List<SellerRegisterRequestDTO> list = sellerService.getSellerProfiles();
    GetSellerProfileResponseDTO response = new GetSellerProfileResponseDTO();
    response.setSuccess(true);
    response.setMessage("Tải thông tin thành công");
    response.setSellerProfileList(list);
    return response;
  }

  public UpdateSellerProfileStatusResponseDTO updateSellerProfileStatus(
          UpdateSellerProfileStatusRequestDTO request) {
    return sellerService.handleUpdateSellerProfileStatus(request);
  }

  public CancelSellerAuctionsResponseDTO cancelSellerAuctions(CancelSellerAuctionsRequestDTO request) {
    boolean success = auctionService.cancelActiveAndWaitingAuctionsBySellerUserId(request.getUserId());
    String message = success
        ? "Đã hủy các phiên đấu giá đang/sắp diễn ra của seller!"
        : "Không thể hủy các phiên đấu giá của seller!";
    return new CancelSellerAuctionsResponseDTO(success, message);
  }

  public RestoreSellerAuctionsResponseDTO restoreSellerAuctions(RestoreSellerAuctionsRequestDTO request) {
    boolean success = auctionService.restoreCanceledAuctionsBySellerUserId(request.getUserId());
    String message = success
        ? "Đã mở lại các phiên đấu giá bị hủy của seller!"
        : "Không thể mở lại các phiên đấu giá của seller!";
    return new RestoreSellerAuctionsResponseDTO(success, message);
  }

  public AutoBidResponseDTO setAutoBid(SetAutoBidRequestDTO req) {
    boolean success = autoBidService.setAutoBid(req);

    if (success) {
      if (req.isActive()) {
        new BidService().resolveAutoBidFight(req.getAuctionId());
      }
      return new AutoBidResponseDTO(true, "Đã cập nhật tự động đấu giá!");
    } else {
      return new AutoBidResponseDTO(false, "Không thể cài đặt tự động đấu giá. Kiểm tra lại giá trị!");
    }
  }

  public AutoBidResponseDTO cancelAutoBid(CancelAutoBidRequestDTO req) {
    boolean success = autoBidService.cancelAutoBid(req);
    return success
            ? new AutoBidResponseDTO(true, "Đã tắt tự động đấu giá!")
            : new AutoBidResponseDTO(false, "Không thể tắt tự động đấu giá!");
  }

  public GetBalanceResponseDTO getBalance(String userId) {
    try {
      BigDecimal currentBalance = walletService.getBalance(userId);
      return new GetBalanceResponseDTO(true, "Lấy số dư thành công", currentBalance);
    } catch (Exception e) {
      log.error("Lỗi khi xử lý số dư trong RequestDispatcher cho user: {}", userId, e);
      return new GetBalanceResponseDTO(false, "Không tìm thấy thông tin ví hoặc lỗi hệ thống", BigDecimal.ZERO);
    }
  }

  public GetNotificationsResponseDTO getNotifications(GetNotificationsRequestDTO req) {
    List<Notification> notifications = notifService.getNotifications(req.getUserId());
    int unreadCount = (int) notifications.stream().filter(n -> !n.isRead()).count();
    return new GetNotificationsResponseDTO(true, notifications, unreadCount);
  }

  public void markNotificationRead(MarkNotificationReadRequestDTO req) {
    if (req.isMarkAll()) {
      notifService.markAllAsRead(req.getUserId());
    } else {
      notifService.markAsRead(req.getNotificationId());
    }
  }

  public GetPendingOrdersOfSellerResponseDTO handleGetPendingOrdersOfSeller(
      GetPendingOrdersOfSellerRequestDTO req) {
    List<OrderDTO> pendingOrders =  orderService.getPendingOrdersBySellerId(req.getSellerId());
    return new GetPendingOrdersOfSellerResponseDTO("Tải danh sách thành công", true, pendingOrders);
  }

  public GetPendingOrdersOfBuyerResponseDTO handleGetPendingOrdersOfBuyer(
      GetPendingOrdersOfBuyerRequestDTO req) {
    List<OrderDTO> pendingOrders =  orderService.getPendingOrdersByBuyerId(req.getBuyerId());
    return new GetPendingOrdersOfBuyerResponseDTO("Tải danh sách thành công", true, pendingOrders);
  }

  public GetCompletedOrdersOfSellerResponseDTO handleGetCompletedOrdersOfSeller(
      GetCompletedOrdersOfSellerRequestDTO req) {
    List<OrderDTO> completedOrders =  orderService.getCompletedOrdersBySellerId(req.getSellerId());
    return new GetCompletedOrdersOfSellerResponseDTO("Tải danh sách thành công", true, completedOrders);
  }

  public GetCancelledOrdersOfSellerResponseDTO handleGetCancelledOrdersOfSeller(
      GetCancelledOrdersOfSellerRequestDTO req) {
    List<OrderDTO> cancelledOrders =  orderService.getCancelledOrdersBySellerId(req.getSellerId());
    return new GetCancelledOrdersOfSellerResponseDTO("Tải danh sách thành công", true, cancelledOrders);
  }

  public GetCompletedOrdersOfBuyerResponseDTO handleGetCompletedOrdersOfBuyer(
      GetCompletedOrdersOfBuyerRequestDTO req) {
    List<OrderDTO> completedOrders =  orderService.getCompletedOrdersByBuyerId(req.getBuyerId());
    return new GetCompletedOrdersOfBuyerResponseDTO("Tải danh sách thành công", true, completedOrders);
  }

  public GetCancelledOrdersOfBuyerResponseDTO handleGetCancelledOrdersOfBuyer(
      GetCancelledOrdersOfBuyerRequestDTO req) {
    List<OrderDTO> cancelledOrders =  orderService.getCancelledOrdersByBuyerId(req.getBuyerId());
    return new GetCancelledOrdersOfBuyerResponseDTO("Tải danh sách thành công", true, cancelledOrders);
  }

  public CreateAdminResponseDTO createAdmin(CreateAdminRequestDTO req) {
    boolean isSuccess = authService.createAdmin(req);
    String msg = isSuccess ? "Đăng ký tài khoản admin thành công!" :
        "Tài khoản đã tồn tại hoặc lỗi hệ thống!";
    return new CreateAdminResponseDTO(isSuccess, msg);
  }

  public CreateTransactionResponseDTO createTransactionRequest(CreateTransactionRequestDTO req) {
    boolean success = walletService.createTransactionRequest(req.getUserId(), req.getAmount(), req.getType());
    String message = success ? "Yêu cầu giao dịch đang được xử lý!" : "Lỗi khi tạo yêu cầu giao dịch!";
    return new CreateTransactionResponseDTO(success, message);
  }

  public GetPendingTransactionsResponseDTO getPendingTransactions(GetPendingTransactionsRequestDTO req) {
    List<WalletTransaction> pendingTransactions = walletService.getPendingTransactions();
    boolean success = pendingTransactions != null;
    String message = success ? "Tải danh sách thành công" : "Lỗi tải danh sách!";
    return new GetPendingTransactionsResponseDTO(success, message, pendingTransactions);
  }

  public ProcessTransactionResponseDTO processTransactionRequest(ProcessTransactionRequestDTO req) {
    boolean success = walletService.processTransactionRequest(req.getTransactionId(), req.getActionStatus());
    String message = success ? "Duyệt giao dịch thành công!" : "Lỗi khi duyệt giao dịch!";
    return new ProcessTransactionResponseDTO(success, message);
  }

  public UpdateAuctionStatusResponseDTO updateAuctionStatus(UpdateAuctionStatusRequestDTO req) {
    return auctionService.updateAuctionStatusByAdmin(req);
  }

  public com.auction.shared.response.GetAllUsersResponseDTO getAllUsers(com.auction.shared.request.GetAllUsersRequestDTO req) {
    try {
      List<UserDTO> users = authService.getAllUsers();
      return new com.auction.shared.response.GetAllUsersResponseDTO(true, "Tải danh sách người dùng thành công!", users);
    } catch (Exception e) {
      log.error("Lỗi khi lấy toàn bộ người dùng trong RequestDispatcher", e);
      return new com.auction.shared.response.GetAllUsersResponseDTO(false, "Lỗi hệ thống: " + e.getMessage(), null);
    }
  }
}
