package com.auction.shared.response;

import com.auction.shared.model.user.UserDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GetAllUsersResponseDTO implements ResponseDTO {
  private String message;
  private boolean success;
  private List<UserDTO> users;

  public GetAllUsersResponseDTO(boolean success, String message, List<UserDTO> users) {
    this.success = success;
    this.message = message;
    this.users = users;
  }
}
