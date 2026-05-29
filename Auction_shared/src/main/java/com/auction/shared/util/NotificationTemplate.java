package com.auction.shared.util;

import com.auction.shared.enums.NotificationType;
import com.auction.shared.model.notification.Notification;

import java.math.BigDecimal;


public class NotificationTemplate {

  // ==================== AUCTION ====================
  public static Notification auctionWon(String userId, String itemName,
                                        BigDecimal finalPrice, String orderId) {
    return new Notification(
        userId,
        NotificationType.AUCTION_WON,
        "Chúc mừng! Bạn đã thắng phiên đấu giá",
        "Sản phẩm: " + itemName + "\n"
            + "Giá cuối: " + CurrencyUtils.formatD(finalPrice) + "\n"
            + "Vui lòng xác nhận thanh toán trong vòng 7 ngày.",
        orderId
    );
  }

  public static Notification auctionEndedWithWinner(String sellerId, String itemName,
                                                    BigDecimal finalPrice, String orderId) {
    return new Notification(
        sellerId,
        NotificationType.AUCTION_ENDED,
        "Phiên đấu giá của bạn đã kết thúc",
        "Sản phẩm: " + itemName + "\n"
            + "Giá bán: " + CurrencyUtils.formatD(finalPrice) + "\n"
            + "Người mua đang trong quá trình xác nhận thanh toán.",
        orderId
    );
  }

  public static Notification auctionEndedNoWinner(String sellerId, String itemName,
                                                  String auctionId) {
    return new Notification(
        sellerId,
        NotificationType.AUCTION_ENDED,
        "Phiên đấu giá kết thúc không có người mua",
        "Sản phẩm: " + itemName + "\n"
            + "Không có ai đặt giá trong phiên này.",
        auctionId
    );
  }

  // ==================== ORDER ====================
  public static Notification orderConfirmedForSeller(String sellerId, String itemName,
                                                     BigDecimal finalPrice, String orderId) {
    return new Notification(
        sellerId,
        NotificationType.ORDER_CONFIRMED,
        "Đơn hàng đã được xác nhận thanh toán",
        "Sản phẩm: " + itemName + "\n"
            + "Số tiền nhận được: " + CurrencyUtils.formatD(finalPrice) + "\n"
            + "Vui lòng chuẩn bị hàng và liên hệ người mua để giao hàng.",
        orderId
    );
  }

  public static Notification orderConfirmedForBuyer(String buyerId, String itemName,
                                                    BigDecimal finalPrice, String orderId) {
    return new Notification(
        buyerId,
        NotificationType.ORDER_CONFIRMED,
        "Thanh toán thành công",
        "Sản phẩm: " + itemName + "\n"
            + "Số tiền đã thanh toán: " + CurrencyUtils.formatD(finalPrice) + "\n"
            + "Người bán sẽ sớm liên hệ để giao hàng.",
        orderId
    );
  }

  public static Notification orderCancelledForSeller(String sellerId, String itemName,
                                                     BigDecimal depositAmount, String orderId) {
    return new Notification(
        sellerId,
        NotificationType.ORDER_CANCELLED_BY_BUYER,
        "Người mua đã hủy đơn hàng",
        "Sản phẩm: " + itemName + "\n"
            + "Tiền cọc bồi thường: " + CurrencyUtils.formatD(depositAmount) + "\n"
            + "Số tiền đã được cộng vào ví của bạn.",
        orderId
    );
  }

  public static Notification orderCancelledForBuyer(String buyerId, String itemName,
                                                    BigDecimal depositAmount, String orderId) {
    return new Notification(
        buyerId,
        NotificationType.ORDER_CANCELLED,
        "Bạn đã hủy đơn hàng",
        "Sản phẩm: " + itemName + "\n"
            + "Tiền cọc bị mất: " + CurrencyUtils.formatD(depositAmount) + "\n"
            + "Lưu ý: Hủy đơn sẽ mất toàn bộ tiền cọc.",
        orderId
    );
  }

  public static Notification orderExpiredForBuyer(String buyerId, String itemName,
                                                  BigDecimal depositAmount, String orderId) {
    return new Notification(
        buyerId,
        NotificationType.ORDER_CANCELLED,
        "Đơn hàng đã bị hủy do quá hạn",
        "Sản phẩm: " + itemName + "\n"
            + "Đơn hàng của bạn đã bị hủy tự động do không xác nhận thanh toán trong 7 ngày.\n"
            + "Tiền cọc bị mất: " + CurrencyUtils.formatD(depositAmount),
        orderId
    );
  }

  public static Notification orderExpiredForSeller(String sellerId, String itemName,
                                                   BigDecimal depositAmount, String orderId) {
    return new Notification(
        sellerId,
        NotificationType.ORDER_CANCELLED,
        "Đơn hàng đã bị hủy do quá hạn",
        "Sản phẩm: " + itemName + "\n"
            + "Người mua không xác nhận thanh toán trong 7 ngày, đơn hàng đã bị hủy tự động.\n"
            + "Tiền cọc bồi thường: " + CurrencyUtils.formatD(depositAmount),
        orderId
    );
  }

  // ==================== SELLER PROFILE ====================
  public static Notification sellerSubmitted(String userId) {
    return new Notification(
        userId,
        NotificationType.REQUEST_SUBMITTED,
        "Yêu cầu mở tài khoản bán hàng ",
        "Yêu cầu mở tài khoản của bạn đã được gửi đi\n"
            + "Hiện tại yêu cầu của bạn đang được Admin xét duyệt."
            + "Chúng tôi sẽ phản hồi trong thời gian sớm nhất.",
        null
    );
  }
  public static Notification sellerApproved(String userId) {
    return new Notification(
        userId,
        NotificationType.REQUEST_APPROVED,
        "Tài khoản người bán đã được duyệt",
        "Chúc mừng! Hồ sơ người bán của bạn đã được phê duyệt.\n"
            + "Bạn có thể bắt đầu đăng sản phẩm lên sàn ngay bây giờ.",
        null
    );
  }

  public static Notification sellerRejected(String userId) {
    return new Notification(
        userId,
        NotificationType.REQUEST_REJECTED,
        "Tài khoản người bán bị từ chối",
        "Hồ sơ người bán của bạn không đáp ứng yêu cầu.\n"
            + "Vui lòng kiểm tra lại thông tin và liên hệ hỗ trợ để biết thêm chi tiết.",
        null
    );
  }

  // ==================== SYSTEM ====================
  public static Notification welcome(String userId) {
    return new Notification(
        userId,
        NotificationType.SYSTEM,
        "Chào mừng bạn đến với Đấu Giá 88!",
        "Tài khoản của bạn đã được tạo thành công.\n"
            + "Khám phá ngay các phiên đấu giá hấp dẫn đang diễn ra!\n"
            + "Chúc bạn có những trải nghiệm mua sắm tuyệt vời.",
        null
    );
  }

  // =================== WALLET ======================
  public static Notification depositSubmitted(String userId, BigDecimal amount) {
    return new Notification(
        userId,
        NotificationType.REQUEST_SUBMITTED,
        "Yêu cầu nạp tiền ",
        "Yêu cầu nạp tiền của bạn đã được gửi đi\n"
            + "Số tiền cần nạp: " + CurrencyUtils.formatD(amount) + "\n"
            + "Hiện tại yêu cầu của bạn đang được Admin xét duyệt."
            + "Chúng tôi sẽ phản hồi trong thời gian sớm nhất.",
        null
    );
  }

  public static Notification withdrawSubmitted(String userId, BigDecimal amount) {
    return new Notification(
        userId,
        NotificationType.REQUEST_SUBMITTED,
        "Yêu cầu rút tiền ",
        "Yêu cầu rút tiền của bạn đã được gửi đi\n"
            + "Số tiền cần rút: " + CurrencyUtils.formatD(amount) + "\n"
            + "Hiện tại yêu cầu của bạn đang được Admin xét duyệt."
            + "Chúng tôi sẽ phản hồi trong thời gian sớm nhất.",
        null
    );
  }

  public static Notification depositApproved(String userId, BigDecimal amount, BigDecimal currentBalance) {
    return new Notification(
        userId,
        NotificationType.REQUEST_APPROVED,
        "Yêu cầu nạp tiền đã được phê duyệt ",
        "Chúc mừng! Yêu cầu nạp tiền của bạn đã được phê duyệt\n"
            + "Số tiền nạp: " + CurrencyUtils.formatD(amount) + "\n"
            + "Số dư hiện tại: " + CurrencyUtils.formatD(currentBalance) + "\n",
        null
    );
  }

  public static Notification withdrawApproved(String userId, BigDecimal amount, BigDecimal currentBalance) {
    return new Notification(
        userId,
        NotificationType.REQUEST_APPROVED,
        "Yêu cầu rút tiền đã được phê duyệt ",
        "Chúc mừng! Yêu cầu rút tiền của bạn đã được phê duyệt\n"
            + "Số tiền rút: " + CurrencyUtils.formatD(amount) + "\n"
            + "Số dư hiện tại: " + CurrencyUtils.formatD(currentBalance) + "\n",
        null
    );
  }

  public static Notification depositRejected(
      String userId,
      BigDecimal amount,
      BigDecimal currentBalance
  ) {

    return new Notification(
        userId,
        NotificationType.REQUEST_REJECTED,
        "Yêu cầu nạp tiền đã bị từ chối",
        "Yêu cầu nạp tiền của bạn chưa được chấp thuận.\n"
            + "Số tiền yêu cầu: " + CurrencyUtils.formatD(amount) + "\n"
            + "Số dư hiện tại: " + CurrencyUtils.formatD(currentBalance) + "\n"
            + "Vui lòng kiểm tra lại thông tin hoặc liên hệ hỗ trợ để biết thêm chi tiết.",
        null
    );
  }

  public static Notification withdrawRejected(
      String userId,
      BigDecimal amount,
      BigDecimal currentBalance
  ) {

    return new Notification(
        userId,
        NotificationType.REQUEST_REJECTED,
        "Yêu cầu rút tiền đã bị từ chối",
        "Yêu cầu rút tiền của bạn chưa được chấp thuận.\n"
            + "Số tiền yêu cầu: " + CurrencyUtils.formatD(amount) + "\n"
            + "Số dư hiện tại: " + CurrencyUtils.formatD(currentBalance) + "\n"
            + "Vui lòng kiểm tra lại thông tin hoặc liên hệ hỗ trợ để biết thêm chi tiết.",
        null
    );
  }
}