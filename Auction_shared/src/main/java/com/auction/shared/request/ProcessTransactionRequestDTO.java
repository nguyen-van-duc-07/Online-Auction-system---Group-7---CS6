package com.auction.shared.request;

import com.auction.shared.enums.WalletTransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTransactionRequestDTO implements RequestDTO {
    private static final long serialVersionUID = 1L;

    private String transactionId;
    private WalletTransactionStatus actionStatus; // APPROVE or REJECT
    private String referenceId;
}
