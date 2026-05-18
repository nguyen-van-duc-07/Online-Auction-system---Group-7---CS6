package com.auction.shared.model.transaction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@ToString(callSuper = true) // In ra cả các thuộc tính của lớp cha (fromId, toId)
public class PrizedTransaction extends Transaction {
  private String auctionId;
  private String itemId;
  private BigDecimal finalPrice;

  public PrizedTransaction(String fromId, String toId, String auctionId, String itemId, BigDecimal finalPrice) {
    // fromId: Người thắng (Người bị trừ tiền)
    // toId: Người bán hoặc Hệ thống (Người nhận tiền)
    super(fromId, toId);

    if (auctionId == null || auctionId.trim().isEmpty()) {
      throw new IllegalArgumentException("Auction ID không được để trống.");
    }
    if (itemId == null || itemId.trim().isEmpty()) {
      throw new IllegalArgumentException("Item ID không được để trống.");
    }
    if (finalPrice == null || finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Giá chốt (finalPrice) phải lớn hơn 0.");
    }
    this.auctionId = auctionId;
    this.itemId = itemId;
    this.finalPrice = finalPrice;
  }
}
/*3. Logic xử lý (Flow quan trọng)
Đây là cách PrizedTransaction kết hợp với Wallet:
Bước 1 (Khi đang đấu giá): Mỗi khi người dùng Bid, bạn dùng
wallet.freeze(amount) để tạm giữ tiền.
Bước 2 (Khi đấu giá kết thúc):
Với người thua: Bạn gọi wallet.unfreeze(amount) để trả lại tiền khả dụng cho họ.
Với người thắng:
Gọi wallet.payWinningBid(amount) (hàm này bạn đã viết để trừ cả tổng và tiền đóng băng).
Khởi tạo một PrizedTransaction để lưu vào lịch sử.
Cập nhật trạng thái món hàng sang "Đã bán".*/