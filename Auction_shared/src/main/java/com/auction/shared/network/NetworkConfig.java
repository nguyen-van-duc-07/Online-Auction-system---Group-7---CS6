package com.auction.shared.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkConfig {
  private static final Logger log = LoggerFactory.getLogger(NetworkConfig.class);

  // Khai báo các thuộc tính mạng dưới dạng biến static non-final để tải động tại runtime
  public static int SERVER_PORT = 8080;
  public static String DEFAULT_HOST = "127.0.0.1";
  public static int IMAGE_SERVER_PORT = 9090;

  static {
    loadConfiguration();
  }

  /**
   * Tải cấu hình mạng từ tệp tin config.properties nằm tại thư mục hiện hành.
   */
  public static void loadConfiguration() {
    File configFile = new File("config.properties");
    if (configFile.exists()) {
      Properties props = new Properties();
      try (FileInputStream fis = new FileInputStream(configFile)) {
        props.load(fis);
        
        String hostVal = props.getProperty("server.host");
        if (hostVal != null && !hostVal.trim().isEmpty()) {
          DEFAULT_HOST = hostVal.trim();
        }

        String portVal = props.getProperty("server.port");
        if (portVal != null && !portVal.trim().isEmpty()) {
          SERVER_PORT = Integer.parseInt(portVal.trim());
        }

        String imgPortVal = props.getProperty("image.server.port");
        if (imgPortVal != null && !imgPortVal.trim().isEmpty()) {
          IMAGE_SERVER_PORT = Integer.parseInt(imgPortVal.trim());
        }

        log.info(">>> Đã tải cấu hình mạng thành công từ config.properties:");
        log.info("    - Server Host: {}", DEFAULT_HOST);
        log.info("    - Server Port: {}", SERVER_PORT);
        log.info("    - Image Port:  {}", IMAGE_SERVER_PORT);
      } catch (IOException | NumberFormatException e) {
        log.error(">>> Lỗi xảy ra khi đọc tệp tin config.properties. Sử dụng cấu hình mặc định (localhost).", e);
      }
    } else {
      log.info(">>> Không tìm thấy tệp tin config.properties tại thư mục gốc. Sử dụng cấu hình mặc định (localhost).");
    }
  }
}

