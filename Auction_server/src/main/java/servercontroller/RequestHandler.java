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
import com.auction.shared.model.transaction.WalletTransaction;
import java.math.BigDecimal;
import java.util.List;

/**
 * Bộ điều hướng trung tâm (Controller) xử lý logic phân nhánh cho các yêu cầu từ Client.
 */
public class RequestHandler {
  private static RequestDispatcher dispatcher = new RequestDispatcher();

  /**
   * Thiết lập dispatcher mới phục vụ mục đích Unit Test Mockito.
   */
  static void setDispatcher(RequestDispatcher newDispatcher) {
    dispatcher = newDispatcher;
  }

  public static LoginResponseDTO login(LoginRequestDTO loginReq) {
    return dispatcher.login(loginReq);
  }

  public static SignUpResponseDTO signup(SignUpRequestDTO signUpReq) {
    return dispatcher.signup(signUpReq);
  }

  public static UploadItemResponseDTO uploadItem(UploadItemRequestDTO uploadItemReq) {
    return dispatcher.uploadItem(uploadItemReq);
  }

  public static AuctionResponseDTO handleFindAuctionById(AuctionRequestDTO request) {
    return dispatcher.handleFindAuctionById(request);
  }

  public static GetActiveAuctionsResponseDTO getActiveAuctions(GetActiveAuctionsRequestDTO getActiveAuctionReq) {
    return dispatcher.getActiveAuctions(getActiveAuctionReq);
  }

  public static GetWaitingAuctionsResponseDTO getWaitingAuctions(GetWaitingAuctionsRequestDTO request) {
    return dispatcher.getWaitingAuctions(request);
  }

  public static GetClosedAuctionsResponseDTO getClosedAuctions(GetClosedAuctionsRequestDTO request) {
    return dispatcher.getClosedAuctions(request);
  }

  public static GetActiveAndWaitingAuctionsResponseDTO getActiveAndWaitingAuctions(
          GetActiveAndWaitingAuctionsRequestDTO request) {
    return dispatcher.getActiveAndWaitingAuctions(request);
  }

  public static GetCanceledAuctionsResponseDTO getCanceledAuctions(
          GetCanceledAuctionsRequestDTO request) {
    return dispatcher.getCanceledAuctions(request);
  }

  public static GetActiveAuctionsBySellerResponseDTO getActiveAuctionsBySeller(
      GetActiveAuctionsBySellerRequestDTO request) {
    return dispatcher.getActiveAuctionsBySeller(request);
  }

  public static GetAuctionsBySellerResponseDTO getAuctionsBySeller(GetAuctionsBySellerRequestDTO request) {
    return dispatcher.getAuctionsBySeller(request);
  }

  public static UpdateProfileResponseDTO updateProfile(UpdateProfileRequestDTO updateProfileReq) {
    return dispatcher.updateProfile(updateProfileReq);
  }

  public static ChangePasswordResponseDTO changePassword(ChangePasswordRequestDTO req) {
    return dispatcher.changePassword(req);
  }

  public static PlaceBidResponseDTO placeBid(PlaceBidRequestDTO req) {
    return dispatcher.placeBid(req);
  }

  public static JoinRoomResponseDTO joinRoom(JoinRoomRequestDTO request, String userId) {
    return dispatcher.joinRoom(request, userId);
  }

  public static SellerRegisterResponseDTO sellerRegister(SellerRegisterRequestDTO sellerRegisterReq) {
    return dispatcher.sellerRegister(sellerRegisterReq);
  }

  public static CheckingSellerProfileResponseDTO checkingSellerProfile(CheckingSellerProfileRequestDTO req) {
    return dispatcher.checkingSellerProfile(req);
  }

  public static OrderActionResponseDTO confirmOrder(ConfirmOrderRequestDTO req) {
    return dispatcher.confirmOrder(req);
  }

  public static OrderActionResponseDTO cancelOrder(CancelOrderRequestDTO req) {
    return dispatcher.cancelOrder(req);
  }

  public static GetOrderResponseDTO getOrder(GetOrderRequestDTO req) {
    return dispatcher.getOrder(req);
  }

  public static GetSellerProfileResponseDTO getSellerProfile(GetSellerProfileRequestDTO request) {
    return dispatcher.getSellerProfile(request);
  }

  public static UpdateSellerProfileStatusResponseDTO updateSellerProfileStatus(
          UpdateSellerProfileStatusRequestDTO request) {
    return dispatcher.updateSellerProfileStatus(request);
  }

  public static CancelSellerAuctionsResponseDTO cancelSellerAuctions(CancelSellerAuctionsRequestDTO request) {
    return dispatcher.cancelSellerAuctions(request);
  }

  public static RestoreSellerAuctionsResponseDTO restoreSellerAuctions(RestoreSellerAuctionsRequestDTO request) {
    return dispatcher.restoreSellerAuctions(request);
  }

  public static AutoBidResponseDTO setAutoBid(SetAutoBidRequestDTO req) {
    return dispatcher.setAutoBid(req);
  }

  public static AutoBidResponseDTO cancelAutoBid(CancelAutoBidRequestDTO req) {
    return dispatcher.cancelAutoBid(req);
  }

  public static GetBalanceResponseDTO getBalance(String userId) {
    return dispatcher.getBalance(userId);
  }

  public static GetNotificationsResponseDTO getNotifications(GetNotificationsRequestDTO req) {
    return dispatcher.getNotifications(req);
  }

  public static void markNotificationRead(MarkNotificationReadRequestDTO req) {
    dispatcher.markNotificationRead(req);
  }

  public static GetPendingOrdersOfSellerResponseDTO handleGetPendingOrdersOfSeller(
      GetPendingOrdersOfSellerRequestDTO req) {
    return dispatcher.handleGetPendingOrdersOfSeller(req);
  }

  public static GetPendingOrdersOfBuyerResponseDTO handleGetPendingOrdersOfBuyer(
      GetPendingOrdersOfBuyerRequestDTO req) {
    return dispatcher.handleGetPendingOrdersOfBuyer(req);
  }

  public static GetCompletedOrdersOfSellerResponseDTO handleGetCompletedOrdersOfSeller(
      GetCompletedOrdersOfSellerRequestDTO req) {
    return dispatcher.handleGetCompletedOrdersOfSeller(req);
  }

  public static GetCancelledOrdersOfSellerResponseDTO handleGetCancelledOrdersOfSeller(
      GetCancelledOrdersOfSellerRequestDTO req) {
    return dispatcher.handleGetCancelledOrdersOfSeller(req);
  }

  public static GetCompletedOrdersOfBuyerResponseDTO handleGetCompletedOrdersOfBuyer(
      GetCompletedOrdersOfBuyerRequestDTO req) {
    return dispatcher.handleGetCompletedOrdersOfBuyer(req);
  }

  public static GetCancelledOrdersOfBuyerResponseDTO handleGetCancelledOrdersOfBuyer(
      GetCancelledOrdersOfBuyerRequestDTO req) {
    return dispatcher.handleGetCancelledOrdersOfBuyer(req);
  }

  public static CreateAdminResponseDTO createAdmin(CreateAdminRequestDTO req) {
    return dispatcher.createAdmin(req);
  }

  public static CreateTransactionResponseDTO createTransactionRequest(CreateTransactionRequestDTO req) {
    return dispatcher.createTransactionRequest(req);
  }

  public static GetPendingTransactionsResponseDTO getPendingTransactions(GetPendingTransactionsRequestDTO req) {
    return dispatcher.getPendingTransactions(req);
  }

  public static ProcessTransactionResponseDTO processTransactionRequest(ProcessTransactionRequestDTO req) {
    return dispatcher.processTransactionRequest(req);
  }

  public static UpdateAuctionStatusResponseDTO updateAuctionStatus(UpdateAuctionStatusRequestDTO req) {
    return dispatcher.updateAuctionStatus(req);
  }

  public static com.auction.shared.response.GetAllUsersResponseDTO getAllUsers(com.auction.shared.request.GetAllUsersRequestDTO req) {
    return dispatcher.getAllUsers(req);
  }
}
