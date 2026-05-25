package service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dịch vụ lưu trữ ảnh sản phẩm lên đĩa cứng phía Server.
 * Mỗi ảnh được đặt tên ngẫu nhiên UUID để tránh trùng lặp.
 */
public class ImageStorageService {
  private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);
  private static final String UPLOAD_DIR = "uploads/images";

  static {
    try {
      Files.createDirectories(Paths.get(UPLOAD_DIR));
    } catch (IOException e) {
      throw new RuntimeException("Không thể tạo thư mục lưu ảnh: " + UPLOAD_DIR, e);
    }
  }

  /**
   * Lưu mảng byte ảnh vào đĩa cứng dưới tên UUID ngẫu nhiên.
   *
   * @param imageBytes     Dữ liệu nhị phân của ảnh
   * @param imageExtension Đuôi file (jpg, png, ...)
   * @return Tên file đã lưu (ví dụ: "a6e39bd0-1234-5678-abcd.jpg")
   */
  public static String saveImage(byte[] imageBytes, String imageExtension) throws IOException {
    String fileName = UUID.randomUUID() + "." + imageExtension;
    Path filePath = Paths.get(UPLOAD_DIR, fileName);
    Files.write(filePath, imageBytes);
    log.info(">>> Đã lưu ảnh: {}", filePath.toAbsolutePath());
    return fileName;
  }

  /** Trả về đường dẫn thư mục lưu ảnh. */
  public static String getUploadDir() {
    return UPLOAD_DIR;
  }
}
