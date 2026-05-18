package com.auction.shared.response;

import com.auction.shared.model.transaction.PrizedTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO implements ResponseDTO, Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String message;
    private PrizedTransaction transaction;
}