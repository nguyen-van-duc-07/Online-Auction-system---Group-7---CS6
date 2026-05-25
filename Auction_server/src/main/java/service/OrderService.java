package service;

import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.InfoDTO;
import com.auction.shared.model.user.ShopInfoDTO;
import com.auction.shared.model.user.User;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.response.OrderUpdateNotificationDTO;
import com.auction.shared.util.FormatUtil;
import com.auction.shared.util.NotificationTemplate;
import config.DatabaseConnection;
import repository.AuctionRepository;
import repository.OrderRepository;
import repository.SellerProfileRepository;
import repository.UserRepository;
import servercontroller.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
  private static final Logger log = LoggerFactory.getLogger(OrderService.class);
  private final OrderRepository orderRepo = new OrderRepository();
  private final AuctionRepository auctionRepo = new AuctionRepository();
  private final UserRepository userRepo = new UserRepository();
  private final WalletService walletService = new WalletService();
  private final SellerProfileRepository sellerProfileRepo = new SellerProfileRepository();
  private final NotificationService notifService = new NotificationService();

  /**
   * Tạo order mới khi phiên đấu giá kết thúc.
   * Được gọi từ AuctionStatusScheduler khi kết thúc.
   */
  public Order createOrder(String auctionId, String buyerId, BigDecimal finalPrice) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        String sellerId = auctionRepo.getSellerIdByAuctionId(auctionId);
        ShopInfoDTO shopInfo = sellerProfileRepo.getShopInfo(sellerId);
        String consigneeName = userRepo.getAccountNameByUserId(buyerId);
        String itemName = auctionRepo.findAuctionById(auctionId).getItem().getName();
        BigDecimal depositAmount = finalPrice.multiply(new BigDecimal("0.1"));
        BigDecimal remainingAmount = finalPrice.subtract(depositAmount);
        Order order = new Order(
            auctionId,
            buyerId,
            sellerId,
            finalPrice,
            depositAmount,
            remainingAmount,
            OrderStatus.PENDING,
            consigneeName,
            shopInfo.getBrandName(),
            shopInfo.getLocation(),
            itemName
        );
        orderRepo.saveOrder(conn, order);
        conn.commit();

        log.info("[ORDER] Tạo order thành công: {} | Buyer: {} | Giá: {} | Cọc: {} | Còn lại: {}",
            order.getId(), buyerId, FormatUtil.fmt(finalPrice), 
            FormatUtil.fmt(depositAmount), FormatUtil.fmt(remainingAmount));
        return order;

      } catch (Exception e) {
        conn.rollback();
        log.error("[ORDER] Tạo order thất bại cho đấu giá: {}", auctionId, e);
        return null;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.error("[ORDER] Lỗi kết nối DB khi tạo order cho đấu giá: {}", auctionId, e);
      return null;
    }
  }

  /**
   * Buyer xác nhận thanh toán.
   */
  public boolean confirmOrder(String orderId, InfoDTO buyerInfo) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        Order order = orderRepo.findById(orderId);
        if (order == null) {
          return false;
        }
        order.setConsigneeName(buyerInfo.getConsigneeName());
        order.setPhoneNumber(buyerInfo.getPhoneNumber());
        order.setAddress(buyerInfo.getAddress());
        walletService.processPayment(conn, order);
        order.confirm();
        orderRepo.updateOrder(conn, order);
        conn.commit();
        log.info("[ORDER] Xác nhận thanh toán thành công cho đơn hàng: {}", orderId);

        // Lấy itemName từ auction
        AuctionResponseDTO auction = auctionRepo.findAuctionById(order.getAuctionId());
        String itemName = auction != null ? auction.getItem().getName() : "Sản phẩm";

        // Thông báo cho seller
        notifService.sendFromNotification(
            NotificationTemplate.orderConfirmedForSeller(
                order.getSellerId(),
                itemName,
                order.getFinalPrice(),
                order.getId()
            )
        );

        // Thông báo cho buyer
        notifService.sendFromNotification(
            NotificationTemplate.orderConfirmedForBuyer(
                order.getBuyerId(),
                itemName,
                order.getFinalPrice(),
                order.getId()
            )
        );
        notifyToSeller(order);
        return true;

      } catch (Exception e) {
        conn.rollback();
        log.error("[ORDER] Xác nhận đơn hàng {} thất bại", orderId, e);
        throw new RuntimeException(e.getMessage(), e);
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.error("[ORDER] Lỗi kết nối DB khi xác nhận đơn hàng: {}", orderId, e);
      throw new RuntimeException("Lỗi cơ sở dữ liệu: " + e.getMessage(), e);
    }
  }
  private boolean cancelOrderInternal(Connection conn, Order order) throws Exception {
    order.cancel();
    orderRepo.updateOrder(conn, order);
    walletService.processCancelPenalty(conn, order);
    return true;
  }

  /**
   * Buyer hủy đơn hàng.
   */
  public boolean cancelOrder(String orderId) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        Order order = orderRepo.findById(orderId);
        if (order == null) {
          return false;
        }
        cancelOrderInternal(conn, order);
        conn.commit();
        log.info("[ORDER] Hủy đơn thành công: {}", orderId);
        AuctionResponseDTO auction = auctionRepo.findAuctionById(order.getAuctionId());
        String itemName = auction != null ? auction.getItem().getName() : "Sản phẩm";

        // Thông báo cho seller
        notifService.sendFromNotification(
            NotificationTemplate.orderCancelledForSeller(
                order.getSellerId(),
                itemName,
                order.getDepositAmount(),
                order.getId()
            )
        );

        // Thông báo cho buyer
        notifService.sendFromNotification(
            NotificationTemplate.orderCancelledForBuyer(
                order.getBuyerId(),
                itemName,
                order.getDepositAmount(),
                order.getId()
            )
        );
        notifyToSeller(order);
        return true;

      } catch (Exception e) {
        conn.rollback();
        log.error("[ORDER] Hủy đơn hàng {} thất bại", orderId, e);
        return false;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.error("[ORDER] Lỗi kết nối DB khi hủy đơn hàng: {}", orderId, e);
      return false;
    }
  }

  /**
   * Tự động hủy các order PENDING quá 7 ngày.
   * Được gọi từ OrderExpiryScheduler.
   */
  public void cancelExpiredOrders() {
    List<Order> expiredOrders = orderRepo.findExpiredPendingOrders(LocalDateTime.now());
    for (Order order : expiredOrders) {
      log.info("[ORDER] Tự động hủy order hết hạn: {}", order.getId());
      try (Connection conn = DatabaseConnection.getConnection()) {
        conn.setAutoCommit(false);
        try {
          cancelOrderInternal(conn, order);
          conn.commit();
          AuctionResponseDTO auction = auctionRepo.findAuctionById(order.getAuctionId());
          String itemName = auction != null ? auction.getItem().getName() : "Sản phẩm";

          notifService.sendFromNotification(
              NotificationTemplate.orderExpiredForBuyer(
                  order.getBuyerId(),
                  itemName,
                  order.getDepositAmount(),
                  order.getId()
              )
          );

          notifService.sendFromNotification(
              NotificationTemplate.orderExpiredForSeller(
                  order.getSellerId(),
                  itemName,
                  order.getDepositAmount(),
                  order.getId()
              )
          );
        } catch (Exception e) {
          conn.rollback();
          log.error("[ORDER] Lỗi khi xử lý tự động hủy order hết hạn: {}", order.getId(), e);
        } finally {
          conn.setAutoCommit(true);
        }
      } catch (SQLException e) {
        log.error("[ORDER] Lỗi kết nối DB khi tự động hủy order hết hạn: {}", order.getId(), e);
      }
    }
  }
  public Order getOrderById(String orderId) {
    return orderRepo.findById(orderId);
  }
  public void notifyToSeller(Order order) {
    log.debug("Gửi thông báo cập nhật đơn hàng cho Seller: Status={}", order.getStatus());
    OrderUpdateNotificationDTO update = new OrderUpdateNotificationDTO(order.getId(), order.getStatus());
    Server.sendToUser(order.getSellerId(), update);
  }

  public List<OrderDTO> getPendingOrdersBySellerId(String sellerId) {
    List<OrderDTO> pendingOrders = orderRepo.getPendingOrdersBySellerId(sellerId);
    return pendingOrders;
  }

  public List<OrderDTO> getPendingOrdersByBuyerId(String buyerId) {
    List<OrderDTO> pendingOrders = orderRepo.getPendingOrdersByBuyerId(buyerId);
    return pendingOrders;
  }

  public List<OrderDTO> getCompletedOrdersBySellerId(String sellerId) {
    return orderRepo.getCompletedOrdersBySellerId(sellerId);
  }

  public List<OrderDTO> getCancelledOrdersBySellerId(String sellerId) {
    return orderRepo.getCancelledOrdersBySellerId(sellerId);
  }

  public List<OrderDTO> getCompletedOrdersByBuyerId(String buyerId) {
    return orderRepo.getCompletedOrdersByBuyerId(buyerId);
  }

  public List<OrderDTO> getCancelledOrdersByBuyerId(String buyerId) {
    return orderRepo.getCancelledOrdersByBuyerId(buyerId);
  }
}