package repository.debug;

import config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class ShowTableItems {

  public static void main(String[] args) {

    String tableName = "items";

    String sql = "SELECT * FROM " + tableName;

    try (
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()
    ) {

      ResultSetMetaData metaData = rs.getMetaData();

      int columnCount = metaData.getColumnCount();

      // Độ rộng mỗi cột
      int columnWidth = 25;

      System.out.println("\n===== TABLE: " + tableName.toUpperCase() + " =====\n");

      // Header
      for (int i = 1; i <= columnCount; i++) {
        System.out.printf("%-" + columnWidth + "s",
            metaData.getColumnName(i));
      }

      System.out.println();

      // Đường kẻ
      for (int i = 1; i <= columnCount; i++) {
        System.out.print("-".repeat(columnWidth));
      }

      System.out.println();

      // Data rows
      while (rs.next()) {

        for (int i = 1; i <= columnCount; i++) {

          String value = rs.getString(i);

          if (value == null) {
            value = "NULL";
          }

          // Cắt bớt chuỗi quá dài
          if (value.length() > 22) {
            value = value.substring(0, 22) + "...";
          }

          System.out.printf("%-" + columnWidth + "s", value);
        }

        System.out.println();
      }

      System.out.println();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}