package com.auction.shared.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTransactionResponseDTO implements ResponseDTO {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
}
