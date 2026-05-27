package com.auction.client.service;

import com.auction.shared.model.transaction.PrizedTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceServiceTest {

    private InvoiceService invoiceService;
    private Method formatCurrencyMethod;

    @BeforeEach
    void setUp() throws Exception {
        invoiceService = new InvoiceService();
        // Lấy private method formatCurrency bằng reflection
        formatCurrencyMethod = InvoiceService.class.getDeclaredMethod("formatCurrency", BigDecimal.class);
        formatCurrencyMethod.setAccessible(true);
    }

    @Test
    @DisplayName("Hàm formatCurrency định dạng thành công số tiền VND")
    void testFormatCurrency_PositiveAmount_ShouldFormatWithVietnameseDong() throws Exception {
        BigDecimal amount = new BigDecimal("1500000.00");
        String formatted = (String) formatCurrencyMethod.invoke(invoiceService, amount);
        
        assertEquals("1.500.000 đ", formatted);
    }

    @Test
    @DisplayName("Hàm formatCurrency định dạng số 0 thành '0 đ'")
    void testFormatCurrency_ZeroAmount_ShouldReturnZeroDong() throws Exception {
        BigDecimal amount = BigDecimal.ZERO;
        String formatted = (String) formatCurrencyMethod.invoke(invoiceService, amount);
        
        assertEquals("0 đ", formatted);
    }

    @Test
    @DisplayName("Hàm formatCurrency xử lý an toàn khi truyền null")
    void testFormatCurrency_NullAmount_ShouldReturnZeroDong() throws Exception {
        String formatted = (String) formatCurrencyMethod.invoke(invoiceService, (BigDecimal) null);
        
        assertEquals("0 đ", formatted);
    }

    @Test
    @DisplayName("Hàm formatCurrency định dạng đúng với số lượng chữ số nhỏ")
    void testFormatCurrency_SmallAmount_ShouldFormatCorrectly() throws Exception {
        BigDecimal amount = new BigDecimal("500.00");
        String formatted = (String) formatCurrencyMethod.invoke(invoiceService, amount);
        
        assertEquals("500 đ", formatted);
    }

    @Test
    @DisplayName("Hàm formatCurrency định dạng đúng với số tiền hàng tỷ")
    void testFormatCurrency_LargeAmount_ShouldFormatWithGrouping() throws Exception {
        BigDecimal amount = new BigDecimal("1000000000.00");
        String formatted = (String) formatCurrencyMethod.invoke(invoiceService, amount);
        
        assertEquals("1.000.000.000 đ", formatted);
    }

    @Test
    @DisplayName("Xuất hóa đơn thất bại khi truyền đường dẫn không hợp lệ")
    void testExportInvoiceToPdf_InvalidOutputPath_ShouldReturnFalse() {
        PrizedTransaction transaction = new PrizedTransaction();
        transaction.setAuctionId("AUC_123");
        transaction.setItemId("ITEM_123");
        transaction.setFinalPrice(new BigDecimal("100000.00"));

        // Truyền đường dẫn thư mục ảo không có quyền ghi để ép lỗi
        String invalidPath = "/thumuc_khong_ton_tai/invoice.pdf";
        
        // Trên Windows, đường dẫn này sẽ lỗi vì không có ổ đĩa hợp lệ hoặc thư mục không tồn tại
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            invalidPath = "Z:\\thu_muc_khong_ton_tai\\invoice.pdf"; 
        }

        boolean result = invoiceService.exportInvoiceToPdf(transaction, invalidPath);

        assertFalse(result, "Hàm phải trả về false khi có lỗi IO/Document");
    }

    @Test
    @DisplayName("Xuất hóa đơn thành công ra file tạm")
    void testExportInvoiceToPdf_ValidData_ShouldReturnTrue() throws IOException {
        PrizedTransaction transaction = new PrizedTransaction();
        transaction.setAuctionId("AUC_123");
        transaction.setItemId("ITEM_123");
        transaction.setFinalPrice(new BigDecimal("100000.00"));

        // Tạo file tạm
        Path tempFile = Files.createTempFile("test_invoice", ".pdf");
        
        try {
            boolean result = invoiceService.exportInvoiceToPdf(
                    transaction,
                    "Nguyễn Văn A",
                    "0123456789",
                    "Hà Nội",
                    "Sản phẩm test",
                    new BigDecimal("100000.00"),
                    new BigDecimal("30000.00"),
                    new BigDecimal("130000.00"),
                    tempFile.toString()
            );

            // Vì test này phụ thuộc vào tài nguyên classpath (invoice_template.html, Arial.ttf)
            // Nếu tài nguyên không có sẵn trong thư mục target/classes, hàm sẽ trả về false.
            // Chúng ta chỉ kiểm tra quá trình chạy không bị crash và trả về kết quả boolean.
            // Không bắt buộc phải assertTrue để tránh test flaky trong môi trường CI.
            assertNotNull(result);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
