package config;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Class được sử dụng để tạo kết nối tới Database.
 */
public class DatabaseConnection {
  private static final String URL =
      "jdbc:mysql://auction-database-tienhoi2007nguyen-b1fd.c.aivencloud.com:28772/"
          + "defaultdb?sslMode=REQUIRED";
  private static final String USER = "avnadmin";
  private static final String PASS = "AVNS_yIEIvG3JqN_2tiF3H6D";

  /**
   * Tạo và trả về kết nối với MySQL database.
   *
   * @return một đối tượng {@link java.sql.Connection}
   * @throws Exception nếu xảy ra lỗi truy cập cơ sở dữ liệu
   */
  public static Connection getConnection() throws Exception {
    // --- THÊM DÒNG NÀY ĐỂ ÉP NẠP DRIVER MYSQL ---
    Class.forName("com.mysql.cj.jdbc.Driver");
    return DriverManager.getConnection(URL, USER, PASS);
  }
}