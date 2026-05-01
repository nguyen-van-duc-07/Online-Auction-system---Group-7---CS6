package com.auction.shared.model.core.exception;

public class InvalidBidException extends RuntimeException {
    public InvalidBidException(String message) {
        super(message);
    }
}
//Dùng khi người dùng đặt giá thấp hơn giá hiện tại hoặc bước giá không hợp lệ