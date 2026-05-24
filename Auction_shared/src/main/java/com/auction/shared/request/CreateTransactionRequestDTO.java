package com.auction.shared.request;

import com.auction.shared.enums.WalletTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequestDTO implements RequestDTO {
    private static final long serialVersionUID = 1L;

    private String userId;
    private BigDecimal amount;
    private WalletTransactionType type;
}
