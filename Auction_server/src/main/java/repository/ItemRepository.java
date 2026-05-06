package repository;

import com.auction.shared.enums.ItemType;
import com.auction.shared.model.item.Item;
import config.DatabaseConnection;

import java.sql.*;

public class ItemRepository {
  public boolean save(Item item) {
    String sql = "INSERT INTO items (id, name, type, description, created_at) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, item.getId());
      ps.setString(2, item.getName());
      // .name() chuyển Enum thành chuỗi (vd: "ELECTRONIC") để lưu xuống DB
      ps.setString(3, item.getType().name());
      ps.setString(4, item.getDescription());
      ps.setTimestamp(5, Timestamp.valueOf(item.getCreatedAt()));

      int rowsAffected = ps.executeUpdate();
      return rowsAffected > 0; // Trả về true nếu Insert thành công ít nhất 1 dòng

    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  // 2. Tìm thông tin chi tiết của một món hàng dựa vào ID
  public Item findById(String id) {
    String sql = "SELECT * FROM items WHERE id = ?";
    Item item = null;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          item = mapResultSetToItem(rs);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return item; // Sẽ trả về null nếu không tìm thấy món hàng
  }

  // Hàm phụ trợ để mapping dữ liệu (đọc từ ResultSet ra Object)
  private Item mapResultSetToItem(ResultSet rs) throws SQLException {
    // Xử lý Enum an toàn
    String typeStr = rs.getString("type");
    ItemType itemType;
    try {
      itemType = (typeStr != null) ? ItemType.valueOf(typeStr.toUpperCase()) : ItemType.OTHER;
    } catch (IllegalArgumentException e) {
      itemType = ItemType.OTHER;
    }

    // Gọi Constructor 5 tham số của Item (đã kế thừa Entity)
    return new Item(
        rs.getString("id"),
        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
        rs.getString("name"),
        itemType,
        rs.getString("description")
    );
  }
}
