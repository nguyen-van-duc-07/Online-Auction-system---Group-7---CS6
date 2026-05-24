package service;

import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.auction.Auction;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.model.user.Bidder;
import com.auction.shared.model.user.InfoDTO;
import com.auction.shared.model.user.ShopInfoDTO;
import com.auction.shared.model.user.User;
import com.auction.shared.response.OrderUpdateNotificationDTO;
import com.auction.shared.util.FormatUtil;
import com.auction.shared.util.NotificationTemplate;
import config.DatabaseConnection;
import repository.AuctionRepository;
import repository.OrderRepository;
import repository.SellerProfileRepository;
import repository.UserRepository;
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

        System.out.println("[ORDER] Tạo order thành công: " + order.getId()
            + " | Buyer: " + buyerId
            + " | Giá: " + FormatUtil.fmt(finalPrice)
            + " | Cọc: " + FormatUtil.fmt(depositAmount)
            + " | So tien can thanh toan " + FormatUtil.fmt(remainingAmount));
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
        System.out.println("[ORDER] Xác nhận thanh toán thành công: " + orderId);

        // Lấy itemName từ auction
        Auction auction = auctionRepo.findAuctionById(order.getAuctionId());
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
        System.out.println("[ORDER] Hủy đơn thành công: " + orderId);
        Auction auction = auctionRepo.findAuctionById(order.getAuctionId());
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
      try (Connection conn = DatabaseConnection.getConnection()) {
        conn.setAutoCommit(false);
        try {
          cancelOrderInternal(conn, order);
          conn.commit();
          Auction auction = auctionRepo.findAuctionById(order.getAuctionId());
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
          e.printStackTrace();
        } finally {
          conn.setAutoCommit(true);
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }
  public Order getOrderById(String orderId) {
    return orderRepo.findById(orderId);
  }
  public void notifyToSeller(Order order) {
    System.out.println("GUI THONG BAO CHO SELLER: " + order.getStatus());
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