package com.auction.shared.request;

import com.auction.shared.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lớp DTO mang yêu cầu Đăng bán sản phẩm từ Client gửi lên Server.
 * <p>
 * Chứa toàn bộ thông tin cần thiết để Server thực hiện đồng thời 2 việc:
 * 1. Khởi tạo và lưu thông tin vật phẩm (Item).
 * 2. Khởi tạo và lưu thông tin phiên đấu giá (Auction) gắn với vật phẩm đó.
 * </p>
 */
@Getter
@AllArgsConstructor
public class UploadItemRequestDTO implements RequestDTO {
  /** Mã định danh của người bán (Lấy từ Session). */
  private String sellerId;
  /** Tên của sản phẩm. */
  private String itemName;
  /** Phân loại sản phẩm (Điện tử, Thời trang,...). */
  private ItemType itemType;
  /** Mô tả chi tiết sản phẩm. */
  private String description;
  /** Giá khởi điểm của phiên đấu giá. */
  private BigDecimal startPrice;
  /** Bước giá nhỏ nhất của phiên đấu giá. */
  private BigDecimal minStepPrice;
  /** Thời gian bắt đầu phiên đấu giá. */
  private LocalDateTime startTime;
  /** Thời gian kết thúc phiên đấu giá. */
  private LocalDateTime endTime;

  /** Dữ liệu nhị phân của ảnh sản phẩm (đọc từ file bằng Files.readAllBytes). */
  private byte[] imageBytes;

  /** Đuôi mở rộng của file ảnh (ví dụ: "jpg", "png"). */
  private String imageExtension;

  /** Thuộc tính động theo từng danh mục. */
  private java.util.Map<String, String> additionalAttributes;
}