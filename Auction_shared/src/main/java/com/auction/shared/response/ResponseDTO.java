package com.auction.shared.response;

import java.io.Serializable;

/**
 * Marker Interface (Giao diện đánh dấu) đại diện cho mọi Phản hồi (Response)
 * được gửi từ Server trả về cho Client.
 * <p>
 * Bất kỳ lớp DTO nào làm nhiệm vụ vận chuyển kết quả xử lý từ Server
 * (ví dụ: {@link LoginResponseDTO}, {@link SignUpResponseDTO}) đều <b>bắt buộc</b>
 * phải triển khai interface này.
 * </p>
 *
 * <h3>Vai trò trong kiến trúc:</h3>
 * <ul>
 *   <li><b>Serialization:</b> Kế thừa {@link Serializable} đảm bảo
 *   mọi gói tin phản hồi đều có thể truyền tải an toàn qua Socket.</li>
 *   <li><b>Smart Routing tại Client:</b> Cho phép Client (lớp {@code ServerConnection})
 *   sử dụng <i>Switch Pattern Matching</i> để tự động phân loại gói tin nhận được
 *   và điều hướng đến hàm xử lý tương ứng một cách an toàn (Type-safe).</li>
 * </ul>
 *
 * @see com.auction.shared.request.RequestDTO
 */
public interface ResponseDTO extends Serializable {
}
