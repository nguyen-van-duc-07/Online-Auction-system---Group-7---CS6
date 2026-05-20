package com.auction.shared.model.auction;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BidRequestDTOTest {

    @Test
    void testBuilderAndGetters() {
        // Chuẩn bị dữ liệu
        Long expectedAuctionId = 100L;
        String expectedBidderId = "user-123";
        BigDecimal expectedBidAmount = new BigDecimal("500000");

        // Tạo đối tượng bằng Builder
        BidRequestDTO dto = BidRequestDTO.builder()
                .auctionId(expectedAuctionId)
                .bidderId(expectedBidderId)
                .bidAmount(expectedBidAmount)
                .build();

        // Kiểm tra dữ liệu
        assertEquals(expectedAuctionId, dto.getAuctionId(), "Auction ID không khớp với giá trị khởi tạo");
        assertEquals(expectedBidderId, dto.getBidderId(), "Bidder ID không khớp với giá trị khởi tạo");
        assertEquals(expectedBidAmount, dto.getBidAmount(), "Số tiền bid không khớp với giá trị khởi tạo");
    }

    @Test
    void testSettersAndNoArgsConstructor() {
        // Khởi tạo bằng constructor mặc định
        BidRequestDTO dto = new BidRequestDTO();

        // Cập nhật dữ liệu
        dto.setAuctionId(200L);
        dto.setBidderId("user-456");
        dto.setBidAmount(new BigDecimal("1000000"));

        // Kiểm tra
        assertEquals(200L, dto.getAuctionId(), "Auction ID không khớp sau khi gọi setter");
        assertEquals("user-456", dto.getBidderId(), "Bidder ID không khớp sau khi gọi setter");
        assertEquals(new BigDecimal("1000000"), dto.getBidAmount(), "Số tiền bid không khớp sau khi gọi setter");
    }

    @Test
    void testSerializationAndDeserialization() throws IOException, ClassNotFoundException {
        // 1. Tạo đối tượng ban đầu
        BidRequestDTO originalDto = BidRequestDTO.builder()
                .auctionId(999L)
                .bidderId("vip-user-001")
                .bidAmount(new BigDecimal("15000000"))
                .build();

        // 2. Tiến hành Serialize (giả lập việc gửi qua Socket bằng cách ghi ra byte array)
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(originalDto);
        objectOutputStream.close();

        byte[] serializedBytes = byteArrayOutputStream.toByteArray();

        // Đảm bảo dữ liệu đã được ghi ra dạng byte
        assertNotNull(serializedBytes, "Dữ liệu serialize không được null");
        assertTrue(serializedBytes.length > 0, "Kích thước mảng byte phải lớn hơn 0");

        // 3. Tiến hành Deserialize (giả lập việc Server nhận dữ liệu từ Socket và đọc lại)
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedBytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        BidRequestDTO deserializedDto = (BidRequestDTO) objectInputStream.readObject();
        objectInputStream.close();

        // 4. Kiểm tra đối tượng sau khi phục hồi
        assertNotNull(deserializedDto, "Đối tượng sau khi deserialize bị null");
        assertEquals(originalDto.getAuctionId(), deserializedDto.getAuctionId(), "Auction ID bị thay đổi sau khi deserialize");
        assertEquals(originalDto.getBidderId(), deserializedDto.getBidderId(), "Bidder ID bị thay đổi sau khi deserialize");
        assertEquals(originalDto.getBidAmount(), deserializedDto.getBidAmount(), "Số tiền bid bị thay đổi sau khi deserialize");
    }
}