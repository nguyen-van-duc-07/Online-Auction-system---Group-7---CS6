package com.auction.shared.model.auction;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BidRequestDTOTest {
    @Test
    void shouldBeSerializable() {
        assertTrue(Serializable.class.isAssignableFrom(BidRequestDTO.class),
                "DTO phải implements Serializable để truyền qua Socket");
    }
}