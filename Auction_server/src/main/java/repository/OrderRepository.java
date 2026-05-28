package repository;

import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.model.user.ShopInfoDTO;
import config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderRepository {
  private static final Logger log = LoggerFactory.getLogger(OrderRepository.class);

  public boolean saveOrder(Connection conn, Order order) {
    String sql = "INSERT INTO orders (id, auction_id, buyer_id, seller_id, final_price, deposit_amount, remaining_amount, consignee_name, phone_number, address, status, item_name, brand_name) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, order.getId());
      ps.setString(2, order.getAuctionId());
      ps.setString(3, order.getBuyerId());
      ps.setString(4, order.getSellerId());
      ps.setBigDecimal(5, order.getFinalPrice());
      ps.setBigDecimal(6, order.getDepositAmount());
      ps.setBigDecimal(7, order.getRemainingAmount());
      ps.setString(8, order.getConsigneeName());
      ps.setString(9, order.getPhoneNumber());
      ps.setString(10, order.getAddress());
      ps.setString(11, order.getStatus().name());
      ps.setString(12, order.getItemName());
      ps.setString(13, order.getBrandName());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lưu đơn hàng ID: {}", order.getId(), e);
      return false;
    }
  }

  public Order findById(String orderId) {
    String sql = "SELECT * FROM orders WHERE id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, orderId);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        return mapRow(rs);
      }
      return null;

    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi tìm kiếm đơn hàng ID: {}", orderId, e);
      return null;
    }
  }

  public Order findByAuctionId(String auctionId) {
    String sql = "SELECT * FROM orders WHERE auction_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, auctionId);
      ResultSet rs = ps.executeQuery();

      if (rs.next()) {
        return mapRow(rs);
      }
      return null;

    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi tìm kiếm đơn hàng cho phiên ID: {}", auctionId, e);
      return null;
    }
  }

  public List<OrderDTO> getOrdersBySellerIdAndStatus(String sellerId, OrderStatus status) {
    List<OrderDTO> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders WHERE seller_id = ? AND status = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, sellerId);
      ps.setString(2, status.name());
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        orders.add(mapResultSetToOrderDTO(rs));
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy đơn hàng đang xử lý của người bán ID: {}", sellerId, e);
    }
    return orders;
  }

  public List<OrderDTO> getOrdersByBuyerIdAndStatus(String buyerId, OrderStatus status) {
    List<OrderDTO> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders WHERE buyer_id = ? AND status = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, buyerId);
      ps.setString(2, status.name());
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        orders.add(mapResultSetToOrderDTO(rs));
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy đơn hàng đang xử lý của người mua ID: {}", buyerId, e);
    }
    return orders;
  }


  // Tìm các order PENDING quá 7 ngày để tự động hủy
  public List<Order> findExpiredPendingOrders(LocalDateTime now) {
    List<Order> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders "
        + "WHERE status = 'PENDING' "
        + "AND created_at <= ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setTimestamp(1, Timestamp.valueOf(now.minusDays(7)));
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        orders.add(mapRow(rs));
      }

    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy đơn hàng hết hạn", e);
    }

    return orders;
  }

  public boolean updateOrder(Connection conn, Order order) {
    String sql = "UPDATE orders SET consignee_name= ?, phone_number = ?, address = ?, status = ?, resolved_at = ? WHERE id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, order.getConsigneeName());
      ps.setString(2, order.getPhoneNumber());
      ps.setString(3, order.getAddress());
      ps.setString(4, order.getStatus().name());
      ps.setTimestamp(5, order.getResolvedAt() != null
          ? Timestamp.valueOf(order.getResolvedAt()) : null);
      ps.setString(6, order.getId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi cập nhật đơn hàng ID: {}", order.getId(), e);
      return false;
    }
  }

  private OrderDTO mapResultSetToOrderDTO(ResultSet rs) throws SQLException {
    OrderDTO orderDTO = new OrderDTO();
    orderDTO.setOrderId(rs.getString("id"));
    orderDTO.setAuctionId(rs.getString("auction_id"));
    orderDTO.setItemName(rs.getString("item_name"));
    orderDTO.setFinalPrice(rs.getBigDecimal("final_price"));
    orderDTO.setStatus(OrderStatus.valueOf(rs.getString("status")));
    orderDTO.setSellerId(rs.getString("seller_id"));
    orderDTO.setBuyerId(rs.getString("buyer_id"));
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      orderDTO.setCreatedAt(createdAt.toLocalDateTime());
    }
    orderDTO.setPhoneNumber(rs.getString("phone_number"));
    orderDTO.setAddress(rs.getString("address"));

    String brandName = rs.getString("brand_name");
    if (brandName == null || brandName.trim().isEmpty()) {
      SellerProfileRepository sellerProfileRepo = new SellerProfileRepository();
      ShopInfoDTO shopInfo = sellerProfileRepo.getShopInfo(orderDTO.getSellerId());
      if (shopInfo != null) {
        brandName = shopInfo.getBrandName();
      }
    }
    orderDTO.setBrandName(brandName);

    String consigneeName = rs.getString("consignee_name");
    if (consigneeName == null || consigneeName.trim().isEmpty()) {
      UserRepository userRepo = new UserRepository();
      consigneeName = userRepo.getAccountNameByUserId(orderDTO.getBuyerId());
    }
    orderDTO.setWinnerName(consigneeName);
    orderDTO.setConsigneeName(consigneeName);

    return orderDTO;
  }

  private Order mapRow(ResultSet rs) throws SQLException {
    Order order = new Order();
    order.setId(rs.getString("id"));
    order.setAuctionId(rs.getString("auction_id"));
    order.setBuyerId(rs.getString("buyer_id"));
    order.setSellerId(rs.getString("seller_id"));
    order.setFinalPrice(rs.getBigDecimal("final_price"));
    order.setDepositAmount(rs.getBigDecimal("deposit_amount"));
    order.setRemainingAmount(rs.getBigDecimal("remaining_amount"));
    order.setConsigneeName(rs.getString("consignee_name"));
    order.setPhoneNumber(rs.getString("phone_number"));
    order.setAddress(rs.getString("address"));
    order.setItemName(rs.getString("item_name"));
    order.setStatus(OrderStatus.valueOf(rs.getString("status")));
    Timestamp createdAt = rs.getTimestamp("created_at");
    if (createdAt != null) {
      order.setCreatedAt(createdAt.toLocalDateTime());
    }
    Timestamp resolvedAt = rs.getTimestamp("resolved_at");
    if (resolvedAt != null) {
      order.setResolvedAt(resolvedAt.toLocalDateTime());
    }
    return order;
  }
}