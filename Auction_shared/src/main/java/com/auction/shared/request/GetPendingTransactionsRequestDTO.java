package com.auction.shared.request;

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
public class GetPendingTransactionsRequestDTO implements RequestDTO {
    private static final long serialVersionUID = 1L;
}
