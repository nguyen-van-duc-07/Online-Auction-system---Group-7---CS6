package com.auction.shared.model.core.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
//Dùng khi người dùng chưa đăng nhập hoặc không có quyền thực hiện thao tác đó