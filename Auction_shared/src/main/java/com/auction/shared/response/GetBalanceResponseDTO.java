package com.auction.shared.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * Gói tin phản hồi chứa kết quả truy vấn số dư ví từ Server trả về Client.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetBalanceResponseDTO implements ResponseDTO{
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private BigDecimal balance;
}