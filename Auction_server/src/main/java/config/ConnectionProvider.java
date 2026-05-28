package config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Trừu tượng hóa việc lấy kết nối cơ sở dữ liệu để hỗ trợ Unit Test với Mockito.
 * Hỗ trợ cú pháp Lambda hoặc Method Reference trong môi trường production (ví dụ: DatabaseConnection::getConnection).
 */
@FunctionalInterface
public interface ConnectionProvider {
    /**
     * Lấy đối tượng kết nối cơ sở dữ liệu.
     *
     * @return Connection đối tượng kết nối SQL
     * @throws SQLException nếu xảy ra lỗi truy cập dữ liệu
     */
    Connection getConnection() throws SQLException;
}
