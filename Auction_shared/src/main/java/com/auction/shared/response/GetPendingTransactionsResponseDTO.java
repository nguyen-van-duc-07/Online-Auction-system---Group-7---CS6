package com.auction.shared.response;

import com.auction.shared.model.transaction.WalletTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPendingTransactionsResponseDTO implements ResponseDTO {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private List<WalletTransaction> pendingTransactions;
}
