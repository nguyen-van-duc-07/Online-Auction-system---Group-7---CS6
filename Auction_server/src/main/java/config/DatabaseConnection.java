package config;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Class được sử dụng để tạo kết nối tới Database.
 */
public class DatabaseConnection {
  private static final String URL =
      "jdbc:mysql://gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/"
      + "auction_db?useSSL=true";
  private static final String USER = "2vF9fvUA1e6SkrG.root";
  private static final String PASS = "KsAN75FtZIeOrn6t";


  /**
   * Khởi tạo hoặc trả về kết nối hiện tại với cơ sở dữ liệu MySQL/TiDB.
   * Phương thức này sẽ kiểm tra xem đối tượng {@code connection} hiện tại có bị
   * {@code null} hoặc đã bị đóng (closed) hay chưa. Nếu có, nó sẽ khởi tạo
   * một kết nối mới thông qua {@link DriverManager}.
   * Nếu không, nó sẽ trả về kết nối đang có sẵn.
   *
   * @return một đối tượng {@link java.sql.Connection} mở tới cơ sở dữ liệu,
   * @code null nếu quá trình kết nối gặp ngoại lệ (Exception).
   **/
  public static Connection getConnection() {
    try {
      return DriverManager.getConnection(URL, USER, PASS);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}