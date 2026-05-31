package com.auction.shared.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LogoutRequestDTO implements RequestDTO {
    private String userId;
}
