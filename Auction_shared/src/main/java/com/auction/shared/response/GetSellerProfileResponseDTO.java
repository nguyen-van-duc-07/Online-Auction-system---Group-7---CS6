package com.auction.shared.response;

import com.auction.shared.request.SellerRegisterRequestDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GetSellerProfileResponseDTO implements ResponseDTO {
  private String message;
  private boolean success;
  private List<SellerRegisterRequestDTO> sellerProfileList;
}
