package com.auction.shared.model.transaction;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PrizedTransaction extends Transaction {
  private String auctionId;
  private String itemId;
  private BigDecimal finalPrice;

  public PrizedTransaction(String fromId, String receiveId, String auctionId, String itemId, BigDecimal finalPrice) {
    // fromId: Người thắng (Người bị trừ tiền)
    // receiveId: Người bán hoặc Hệ thống (Người nhận tiền)
    super(fromId, receiveId);

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
  @Override

  public void showInfo() {
    System.out.println("--- HÓA ĐƠN THẮNG CUỘC ---");
    System.out.println("Người thắng: " + getFromId());
    System.out.println("Món hàng: " + itemId);
    System.out.println("Giá chốt: " + finalPrice);
    System.out.println("--------------------------");
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