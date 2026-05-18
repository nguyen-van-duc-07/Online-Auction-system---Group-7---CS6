package com.auction.shared.request;

import java.io.Serial;

/**
 * Gói tin yêu cầu lấy số dư ví hiện tại của người dùng.
 * * Áp dụng nguyên tắc Lập trình phòng thủ (Defensive Programming):
 * Class này cố tình không chứa thuộc tính userId để ngăn chặn lỗi bảo mật IDOR.
 * Server sẽ tự động định danh người dùng thông qua luồng kết nối hiện tại.
 */
public class GetBalanceRequestDTO implements RequestDTO{
    @Serial
    private static final long serialVersionUID = 1L;

    // Không cần khai báo field nào ở đây.

    public GetBalanceRequestDTO() {
        // Constructor rỗng mặc định để phục vụ việc tuần tự hóa (Serialization)
    }
}