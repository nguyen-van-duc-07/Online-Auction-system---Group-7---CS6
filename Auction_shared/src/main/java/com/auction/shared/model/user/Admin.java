package com.auction.shared.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor // Cần thiết nếu sau này dùng các thư viện Map dữ liệu
public class Admin extends User {
  public Admin(UserDTO dto) {
    super(dto);
  }
  public void cancelAuction(String auctionId, String reason) {
    // Trong Model, chúng ta chủ yếu xác thực quyền hoặc ghi log nghiệp vụ đơn giản
    System.out.println("Admin " + this.accountName + " đã yêu cầu hủy phiên: " + auctionId + ". Lý do: " + reason);

    // Sau này khi sang Spring Boot, AdminService sẽ gọi hàm này và thực hiện
    // rollback tiền cọc của tất cả Bidder đang tham gia phiên này.
  }

  public void banBidder(String bidderId) {
    // Logic khóa tài khoản
    System.out.println("Admin đã khóa người dùng: " + bidderId);
  }

  @Override
  public String getDefaultAccountName() {
    return "admin" + this.id.substring(0,6);
  }
}