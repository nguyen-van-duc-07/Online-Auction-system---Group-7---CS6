package service;

import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.model.user.User;
import com.auction.shared.response.OrderUpdateNotificationDTO;
import config.DatabaseConnection;
import repository.AuctionRepository;
import repository.OrderRepository;
import repository.SellerProfileRepository;
import repository.UserRepository;
import repository.debug.Format;
import servercontroller.Server;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
  private final OrderRepository orderRepo = new OrderRepository();
  private final AuctionRepository auctionRepo = new AuctionRepository();
  private final UserRepository userRepo = new UserRepository();
  private final WalletService walletService = new WalletService();
  private final SellerProfileRepository sellerProfileRepo = new SellerProfileRepository();

  /**
   * Tạo order mới khi phiên đấu giá kết thúc.
   * Được gọi từ AuctionStatusScheduler khi kết thúc.
   */
  public Order createOrder(String auctionId, String buyerId, BigDecimal finalPrice) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        String sellerProfileId = auctionRepo.getSellerProfileIdByAuctionId(auctionId);
        User buyer = userRepo.getUserByAccountNameOrId(null, buyerId);
        String consigneeName = buyer.getRealName();
        String phoneNumber   = buyer.getPhoneNumber();
        String address       = buyer.getAddress();
        BigDecimal depositAmount = finalPrice.multiply(new BigDecimal("0.1"));
        Order order = new Order(
            auctionId,
            buyerId,
            sellerProfileId,
            finalPrice,
            depositAmount,
            OrderStatus.PENDING,
            consigneeName,
            phoneNumber,
            address
        );
        orderRepo.saveOrder(conn, order);
        conn.commit();

        System.out.println("[ORDER] Tạo order thành công: " + order.getId()
            + " | Buyer: " + buyerId
            + " | Giá: " + Format.fmt(finalPrice)
            + " | Cọc: " + Format.fmt(depositAmount));
        return order;

      } catch (Exception e) {
        conn.rollback();
        System.out.println("[ORDER] Tạo order thất bại: " + e.getMessage());
        e.printStackTrace();
        return null;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Buyer xác nhận thanh toán.
   */
  public boolean confirmOrder(String orderId) {
    try (Connection conn = DatabaseConnection.getConnection()) {
      conn.setAutoCommit(false);
      try {
        Order order = orderRepo.findById(orderId);
        if (order == null) {
          return false;
        }

        order.confirm();
        orderRepo.updateOrder(conn, order);
        walletService.processPayment(conn, order);

        conn.commit();
        System.out.println("[ORDER] Xác nhận thanh toán thành công: " + orderId);
        notifyToSeller(order);
        return true;

      } catch (Exception e) {
        conn.rollback();
        System.out.println("[ORDER] Xác nhận thất bại: " + e.getMessage());
        e.printStackTrace();
        return false;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
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

        order.cancel();
        orderRepo.updateOrder(conn, order);
        walletService.processCancelPenalty(conn, order);

        conn.commit();
        System.out.println("[ORDER] Hủy đơn thành công: " + orderId);
        notifyToSeller(order);
        return true;

      } catch (Exception e) {
        conn.rollback();
        System.out.println("[ORDER] Hủy đơn thất bại: " + e.getMessage());
        e.printStackTrace();
        return false;
      } finally {
        conn.setAutoCommit(true);
      }
    } catch (SQLException e) {
      e.printStackTrace();
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
      System.out.println("[ORDER] Tự động hủy order hết hạn: " + order.getId());
      cancelOrder(order.getId());
    }
  }
  public Order getOrderById(String orderId) {
    return orderRepo.findById(orderId);
  }
  public void notifyToSeller(Order order) {
    System.out.println("GUI THONG BAO CHO SELLER: " + order.getStatus());
    OrderUpdateNotificationDTO update = new OrderUpdateNotificationDTO(order.getId(), order.getStatus());
    String sellerId = sellerProfileRepo.getUserIdByProfileId(order.getSellerProfileId());
    Server.sendToUser(sellerId, update);
  }

  public List<OrderDTO> getPendingOrdersBySellerId(String sellerId) {
    List<OrderDTO> pendingOrders = orderRepo.getPendingOrdersBySellerId(sellerId);
    return pendingOrders;
  }

  public List<OrderDTO> getPendingOrdersByBuyerId(String buyerId) {
    List<OrderDTO> pendingOrders = orderRepo.getPendingOrdersByBuyerId(buyerId);
    return pendingOrders;
  }
}