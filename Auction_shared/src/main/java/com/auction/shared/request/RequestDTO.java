package com.auction.shared.request;

import java.io.Serializable;

/**
 * Marker Interface đại diện cho mọi Yêu cầu (Request) gửi từ Client lên Server.
 * Tất cả các DTO giao tiếp mạng bắt buộc phải triển khai interface này.
 *
 * <p><b>Vai trò cốt lõi:</b></p>
 * <ul>
 *   <li><b>Serialization:</b> Kế thừa {@link Serializable} để đóng gói, truyền tải qua Socket.</li>
 *   <li><b>Smart Routing:</b> Hỗ trợ Server sử dụng <i>Switch Pattern Matching (Java 21)</i>
 *   để phân loại và ép kiểu gói tin tự động, loại bỏ hoàn toàn if-else.</li>
 * </ul>
 *
 * @see LoginRequestDTO
 * @see SignUpRequestDTO
 */
public interface RequestDTO extends Serializable {
}
