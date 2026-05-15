package repository;

import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.order.Order;
import config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

  public boolean saveOrder(Connection conn, Order order) {
    String sql = "INSERT INTO orders (id, auction_id, buyer_id, seller_profile_id, final_price, deposit_amount, consignee_name, phone_number, address, status) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, order.getId());
      ps.setString(2, order.getAuctionId());
      ps.setString(3, order.getBuyerId());
      ps.setString(4, order.getSellerProfileId());
      ps.setBigDecimal(5, order.getFinalPrice());
      ps.setBigDecimal(6, order.getDepositAmount());
      ps.setString(7, order.getConsigneeName());
      ps.setString(8, order.getPhoneNumber());
      ps.setString(9, order.getAddress());
      ps.setString(10, order.getStatus().name());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      e.printStackTrace();
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
      e.printStackTrace();
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
      e.printStackTrace();
      return null;
    }
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
      e.printStackTrace();
    }

    return orders;
  }

  public boolean updateOrder(Connection conn, Order order) {
    String sql = "UPDATE orders SET status = ?, resolved_at = ? WHERE id = ?";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, order.getStatus().name());
      ps.setTimestamp(2, order.getResolvedAt() != null
          ? Timestamp.valueOf(order.getResolvedAt()) : null);
      ps.setString(3, order.getId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  private Order mapRow(ResultSet rs) throws SQLException {
    Order order = new Order();
    order.setId(rs.getString("id"));
    order.setAuctionId(rs.getString("auction_id"));
    order.setBuyerId(rs.getString("buyer_id"));
    order.setSellerProfileId(rs.getString("seller_profile_id"));
    order.setFinalPrice(rs.getBigDecimal("final_price"));
    order.setDepositAmount(rs.getBigDecimal("deposit_amount"));
    order.setConsigneeName(rs.getString("consignee_name"));
    order.setPhoneNumber(rs.getString("phone_number"));
    order.setAddress(rs.getString("address"));
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