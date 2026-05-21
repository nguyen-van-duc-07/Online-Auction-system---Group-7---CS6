package com.auction.shared.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor

public class AutoBidResponseDTO implements ResponseDTO {
  private static final long serialVersionUID = 1L;
  private boolean success;
  private String message;

}