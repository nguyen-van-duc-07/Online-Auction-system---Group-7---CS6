package com.auction.shared.model.core.exception;

public class AuctionClosedException extends RuntimeException {
    public AuctionClosedException(String message) {
        super(message);
    }
}
//Dùng khi phiên đấu giá đã kết thúc hoặc bị hủy mà người dùng vẫn cố tình đặt giá