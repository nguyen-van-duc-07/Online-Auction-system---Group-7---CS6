package service;

import com.sun.net.httpserver.HttpServer;
import com.auction.shared.network.NetworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;

/**
 * HTTP Server siêu nhẹ nhúng vào ứng dụng Server.
 * Phục vụ duy nhất các file ảnh tĩnh trong thư mục uploads/images/.
 * Client JavaFX sử dụng URL dạng: http://host:9090/images/filename.jpg
 */
public class ImageHttpServer {
  private static final Logger log = LoggerFactory.getLogger(ImageHttpServer.class);

  /**
   * Khởi chạy HTTP Static File Server trên port được cấu hình.
   */
  public static void start() throws IOException {
    HttpServer server = HttpServer.create(
        new InetSocketAddress(NetworkConfig.IMAGE_SERVER_PORT), 0);

    server.createContext("/images", exchange -> {
      String requestPath = exchange.getRequestURI().getPath();
      // Lấy tên file từ path: /images/abc.jpg -> abc.jpg
      String fileName = requestPath.substring("/images/".length());

      // Bảo mật: Chặn path traversal (../)
      if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
        exchange.sendResponseHeaders(403, -1);
        exchange.close();
        return;
      }

      Path filePath = Paths.get(ImageStorageService.getUploadDir(), fileName);
      if (Files.exists(filePath)) {
        byte[] fileBytes = Files.readAllBytes(filePath);
        // Set Content-Type dựa vào đuôi file
        String contentType = getContentType(fileName);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, fileBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(fileBytes);
        }
      } else {
        exchange.sendResponseHeaders(404, -1);
      }
      exchange.close();
    });

    server.setExecutor(null); // Sử dụng default executor
    server.start();
    log.info(">>> IMAGE HTTP SERVER ĐANG CHẠY TRÊN PORT: {}", NetworkConfig.IMAGE_SERVER_PORT);
  }

  /**
   * Xác định Content-Type dựa vào đuôi mở rộng file.
   */
  private static String getContentType(String fileName) {
    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
    if (fileName.endsWith(".png")) return "image/png";
    if (fileName.endsWith(".gif")) return "image/gif";
    return "application/octet-stream";
  }
}
