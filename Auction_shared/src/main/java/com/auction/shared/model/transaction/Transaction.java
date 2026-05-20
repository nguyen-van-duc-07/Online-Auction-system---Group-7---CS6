package com.auction.shared.model.transaction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@NoArgsConstructor
public abstract class Transaction {
  private String fromId;
  private String toId;

  public Transaction(String fromId, String toId) {
    Objects.requireNonNull(fromId, "fromId must not be null");
    Objects.requireNonNull(toId, "toId must not be null");
    this.fromId = fromId;
    this.toId = toId;
  }
}

/*Phân tích cấu trúc hiện tại
Abstract Transaction: Có fromId và toId là chuẩn. Mọi giao dịch đều cần thực thể gửi và nhận.
WalletTransaction: Dùng để ghi lại lịch sử Nạp/Rút/Chuyển tiền. Rất tốt khi có TransactionType để phân loại.
BidTransaction: Dùng cho lịch sử đặt giá. Việc tách riêng này giúp bạn dễ dàng truy vấn "Lịch sử đấu giá của
món đồ X" mà không bị lẫn với lịch sử nạp tiền.
PrizedTransaction: Đây là giao dịch khi cuộc đấu giá kết thúc (thanh toán hóa đơn thắng cuộc).*/