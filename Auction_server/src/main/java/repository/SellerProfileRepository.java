package repository;

import com.auction.shared.model.user.SellerProfile;
import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SellerProfileRepository {
  public boolean createSellerProfile(SellerProfile sellerProfile){
    try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "INSERT INTO seller_profiles (id, user_id) VALUES (?, ?)";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, sellerProfile.getId());
      ps.setString(2, sellerProfile.getManagerId());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}

