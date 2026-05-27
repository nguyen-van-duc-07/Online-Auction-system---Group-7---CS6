package com.auction.shared.util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FormatUtilTest {

    @Test
    @DisplayName("Hàm fmt() định dạng thành công số tiền dương lớn")
    void testFmt_PositiveAmount_ShouldFormatWithVndSuffix() {
        BigDecimal amount = new BigDecimal("1500000.00");
        String formatted = FormatUtil.fmt(amount);
        
        // Chấp nhận cả dấu chấm hoặc phẩy tùy locale, nhưng phải có hậu tố " đ" và chứa "1", "5", "0"
        assertTrue(formatted.endsWith(" đ"));
        // Cụ thể, định dạng "%,.0f" với Locale mặc định (thường là US/VN trên máy chạy)
        // sẽ trả về "1.500.000 đ" hoặc "1,500,000 đ"
        String expectedDot = "1.500.000 đ";
        String expectedComma = "1,500,000 đ";
        assertTrue(formatted.equals(expectedDot) || formatted.equals(expectedComma), 
                "Kết quả định dạng thực tế là: " + formatted);
    }

    @Test
    @DisplayName("Hàm fmt() định dạng thành công số 0")
    void testFmt_ZeroAmount_ShouldFormatCorrectly() {
        BigDecimal amount = BigDecimal.ZERO;
        String formatted = FormatUtil.fmt(amount);
        
        assertEquals("0 đ", formatted);
    }

    @Test
    @DisplayName("Hàm fmt() định dạng thành công số tiền âm")
    void testFmt_NegativeAmount_ShouldFormatCorrectly() {
        BigDecimal amount = new BigDecimal("-50000.00");
        String formatted = FormatUtil.fmt(amount);
        
        String expectedDot = "-50.000 đ";
        String expectedComma = "-50,000 đ";
        assertTrue(formatted.equals(expectedDot) || formatted.equals(expectedComma), 
                "Kết quả định dạng thực tế là: " + formatted);
    }

    @Test
    @DisplayName("Hàm fmt() định dạng giá trị null thành ' đ'")
    void testFmt_NullValue_ShouldReturnNullString() {
        String formatted = FormatUtil.fmt(null);
        assertEquals(" đ", formatted);
    }
}
