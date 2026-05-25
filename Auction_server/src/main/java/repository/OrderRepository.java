package repository;

import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.order.OrderDTO;
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
    String sql = "INSERT INTO orders (id, auction_id, buyer_id, seller_id, final_price, deposit_amount, remaining_amount, consignee_name, phone_number, address, status, item_name) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

  public List<OrderDTO> getPendingOrdersBySellerId(String sellerId) {
    List<OrderDTO> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders WHERE seller_id = ? AND status = 'PENDING'";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, sellerId);
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        orders.add(mapResultSetToOrderDTO(rs));
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy đơn hàng đang xử lý của người bán ID: {}", sellerId, e);
    }
    return orders;
  }

  public List<OrderDTO> getPendingOrdersByBuyerId(String buyerId) {
    List<OrderDTO> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders WHERE buyer_id = ? AND status = 'PENDING'";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, buyerId);
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        orders.add(mapResultSetToOrderDTO(rs));
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy đơn hàng đang xử lý của người mua ID: {}", buyerId, e);
    }
    return orders;
  }

  public List<OrderDTO> getCompletedOrdersBySellerId(String sellerId) {
    List<OrderDTO> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders WHERE seller_id = ? AND status = 'CONFIRMED'";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, sellerId);
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        orders.add(mapResultSetToOrderDTO(rs));
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy đơn hàng thành công của người bán ID: {}", sellerId, e);
    }
    return orders;
  }

  public List<OrderDTO> getCancelledOrdersBySellerId(String sellerId) {
    List<OrderDTO> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders WHERE seller_id = ? AND status = 'CANCELLED'";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, sellerId);
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        orders.add(mapResultSetToOrderDTO(rs));
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy đơn hàng bị hủy của người bán ID: {}", sellerId, e);
    }
    return orders;
  }

  public List<OrderDTO> getCompletedOrdersByBuyerId(String buyerId) {
    List<OrderDTO> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders WHERE buyer_id = ? AND status = 'CONFIRMED'";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, buyerId);
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        orders.add(mapResultSetToOrderDTO(rs));
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy đơn hàng thành công của người mua ID: {}", buyerId, e);
    }
    return orders;
  }

  public List<OrderDTO> getCancelledOrdersByBuyerId(String buyerId) {
    List<OrderDTO> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders WHERE buyer_id = ? AND status = 'CANCELLED'";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, buyerId);
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        orders.add(mapResultSetToOrderDTO(rs));
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lấy đơn hàng bị hủy của người mua ID: {}", buyerId, e);
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
    orderDTO.setBrandName(rs.getString("brand_name"));
    orderDTO.setItemName(rs.getString("item_name"));
    orderDTO.setFinalPrice(rs.getBigDecimal("final_price"));
    orderDTO.setStatus(OrderStatus.valueOf(rs.getString("status")));
    orderDTO.setWinnerName(rs.getString("consignee_name"));

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
    order.setDepositAmount(rs.getBigDecimal("remaining_amount"));
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