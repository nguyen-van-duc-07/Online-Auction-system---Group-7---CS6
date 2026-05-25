package config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Class được sử dụng để tạo kết nối tới Database.
 */
public class DatabaseConnection {
  private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);
  private static final String URL =
      "jdbc:mysql://gateway01.ap-southeast-1.prod.alicloud.tidbcloud.com:4000/"
      + "auction_db?useSSL=true";
  private static final String USER = "2vF9fvUA1e6SkrG.root";
  private static final String PASS = "KsAN75FtZIeOrn6t";

  private static final  HikariDataSource dataSource;

  // Hieu co ban la khoi khoi tao ra connect pool
  static {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(URL);
    config.setUsername(USER);
    config.setPassword(PASS);

    // Pool size
    config.setMinimumIdle(5);        // luôn duy trì 5 connection sẵn sàng
    config.setMaximumPoolSize(20);   // tối đa 20 connection đồng thời

    // Timeout
    config.setConnectionTimeout(3000);   // chờ lấy connection tối đa 3s
    config.setIdleTimeout(300000);        // connection idle 5 phút thì đóng
    config.setMaxLifetime(600000);        // connection sống tối đa 10 phút

    // Keepalive — tránh TiDB đóng connection do idle
    config.setKeepaliveTime(60000);       // ping mỗi 1 phút
    config.setConnectionTestQuery("SELECT 1");

    dataSource = new HikariDataSource(config);
    log.info("[DB] Connection pool đã khởi tạo thành công.");
  }


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
      return dataSource.getConnection();
    } catch (Exception e) {
      log.error("Lỗi lấy kết nối từ Connection Pool", e);
      return null;
    }
  }
}