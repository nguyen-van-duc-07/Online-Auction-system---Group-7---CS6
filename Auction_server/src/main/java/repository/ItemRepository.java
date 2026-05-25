package repository;

import com.auction.shared.enums.ItemType;
import com.auction.shared.model.item.Item;
import com.auction.shared.model.item.ItemDTO;
import config.DatabaseConnection;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemRepository {
  private static final Logger log = LoggerFactory.getLogger(ItemRepository.class);
  public boolean saveItem(Item item) {

    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "INSERT INTO items (id, name, type, description) VALUES (?, ?, ?, ?)";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, item.getId());
      ps.setString(2, item.getName());
      ps.setString(3, item.getType().name());
      ps.setString(4, item.getDescription());
      return ps.executeUpdate() > 0; // Trả về true nếu Insert thành công ít nhất 1 dòng

    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi lưu sản phẩm ID: {}", item.getId(), e);
      return false;
    }
  }

  // 2. Tìm thông tin chi tiết của một món hàng dựa vào ID
  public ItemDTO findById(String id) {
    String sql = "SELECT * FROM items WHERE id = ?";
    ItemDTO item = null;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          item = mapResultSetToItem(rs);
        }
      }
    } catch (SQLException e) {
      log.error("Lỗi cơ sở dữ liệu khi tìm kiếm sản phẩm ID: {}", id, e);
    }
    return item; // Sẽ trả về null nếu không tìm thấy món hàng
  }

  // Hàm phụ trợ để mapping dữ liệu (đọc từ ResultSet ra Object)
  private ItemDTO mapResultSetToItem(ResultSet rs) throws SQLException {
    // Xử lý Enum an toàn
    String typeStr = rs.getString("type");
    ItemType itemType;
    try {
      itemType = (typeStr != null) ? ItemType.valueOf(typeStr.toUpperCase()) : ItemType.OTHER;
    } catch (IllegalArgumentException e) {
      itemType = ItemType.OTHER;
    }

    ItemDTO itemDTO = new ItemDTO();
    itemDTO.setId(rs.getString("id"));
    itemDTO.setName(rs.getString("name"));
    itemDTO.setDescription(rs.getString("description"));
    itemDTO.setType(itemType);
    return itemDTO;
  }
}
