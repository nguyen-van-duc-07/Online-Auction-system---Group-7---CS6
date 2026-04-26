package com.auction.shared.model.transaction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Transaction {
    private String fromId;
    private String receiveId;

    public Transaction(String fromId, String receiveId) {
        this.fromId = fromId;
        this.receiveId = receiveId;
    }
    abstract public void showInfo();
}

/*Phân tích cấu trúc hiện tại
Abstract Transaction: Có fromId và receiveId là chuẩn. Mọi giao dịch đều cần thực thể gửi và nhận.
WalletTransaction: Dùng để ghi lại lịch sử Nạp/Rút/Chuyển tiền. Rất tốt khi có TransactionType để phân loại.
BidTransaction: Dùng cho lịch sử đặt giá. Việc tách riêng này giúp bạn dễ dàng truy vấn "Lịch sử đấu giá của
món đồ X" mà không bị lẫn với lịch sử nạp tiền.
PrizedTransaction: Đây là giao dịch khi cuộc đấu giá kết thúc (thanh toán hóa đơn thắng cuộc).*/
