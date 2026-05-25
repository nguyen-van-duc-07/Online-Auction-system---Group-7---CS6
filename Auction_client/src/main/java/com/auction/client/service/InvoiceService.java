package com.auction.client.service;

import com.auction.shared.model.transaction.PrizedTransaction;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceService {
  private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

  // Định dạng tiền tệ VND chuyên nghiệp
  private String formatCurrency(BigDecimal amount) {
    if (amount == null) return "0 đ";
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
    symbols.setGroupingSeparator('.');
    DecimalFormat currencyFormat = new DecimalFormat("#,###", symbols);
    return currencyFormat.format(amount) + " đ";
  }

  /**
   * Phương thức cũ (Giữ nguyên để tương thích ngược nếu cần)
   */
  public boolean exportInvoiceToPdf(PrizedTransaction transaction, String outputPath) {
    return exportInvoiceToPdf(
        transaction,
        "N/A",
        "N/A",
        "N/A",
        "Sản phẩm đấu giá",
        transaction.getFinalPrice(),
        new BigDecimal("30000.00"),
        transaction.getFinalPrice().add(new BigDecimal("30000.00")),
        outputPath
    );
  }

  /**
   * Phương thức xuất hóa đơn chi tiết ra file PDF với đầy đủ dữ liệu người mua và sản phẩm.
   */
  public boolean exportInvoiceToPdf(
      PrizedTransaction transaction,
      String consigneeName,
      String phoneNumber,
      String address,
      String itemName,
      BigDecimal finalPrice,
      BigDecimal shippingFee,
      BigDecimal totalAmount,
      String outputPath
  ) {
    try {
      // 1. Đọc file template HTML từ tài nguyên
      ClassLoader classLoader = getClass().getClassLoader();
      try (InputStream inputStream = classLoader.getResourceAsStream("invoice_template.html")) {
        if (inputStream == null) {
          throw new IllegalArgumentException("Không tìm thấy file template invoice_template.html!");
        }

        String htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        // Định dạng ngày giờ lập hóa đơn thực tế
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentDateStr = dateFormat.format(new Date());

        // 2. Trộn dữ liệu vào HTML (Thay thế các placeholder)
        htmlContent = htmlContent
            .replace("{{date}}", currentDateStr)
            .replace("{{auctionId}}", transaction.getAuctionId() != null ? transaction.getAuctionId() : "N/A")
            .replace("{{transactionId}}", "TXN" + System.currentTimeMillis())
            .replace("{{consigneeName}}", consigneeName != null && !consigneeName.isEmpty() ? consigneeName : "N/A")
            .replace("{{phoneNumber}}", phoneNumber != null && !phoneNumber.isEmpty() ? phoneNumber : "N/A")
            .replace("{{address}}", address != null && !address.isEmpty() ? address : "N/A")
            .replace("{{itemId}}", transaction.getItemId() != null ? transaction.getItemId() : "N/A")
            .replace("{{itemName}}", itemName != null && !itemName.isEmpty() ? itemName : "Sản phẩm đấu giá")
            .replace("{{itemPrice}}", formatCurrency(finalPrice))
            .replace("{{subTotal}}", formatCurrency(finalPrice))
            .replace("{{shippingFee}}", formatCurrency(shippingFee))
            .replace("{{totalAmount}}", formatCurrency(totalAmount));

        // 3. Tiến hành render chuỗi HTML hoàn chỉnh thành file PDF
        try (OutputStream os = new FileOutputStream(outputPath)) {
          PdfRendererBuilder builder = new PdfRendererBuilder();
          builder.useFastMode();

          // KHẮC PHỤC LỖI TIẾNG VIỆT: Tải font Arial.ttf từ tài nguyên classpath thông qua tệp tạm
          // Cách này tránh sử dụng trực tiếp FSSupplier (giải quyết lỗi Java Module JPMS)
          File tempFontFile = File.createTempFile("Arial", ".ttf");
          tempFontFile.deleteOnExit();
          try (InputStream is = classLoader.getResourceAsStream("fonts/Arial.ttf");
               OutputStream fos = new FileOutputStream(tempFontFile)) {
            if (is == null) {
              throw new IllegalArgumentException("Không tìm thấy font Arial.ttf trong tài nguyên!");
            }
            is.transferTo(fos);
          }

          builder.useFont(tempFontFile, "Arial");
          log.info("Đã đăng ký font Arial từ tệp tạm để hiển thị tiếng Việt đa nền tảng.");

          builder.withHtmlContent(htmlContent, "/");
          builder.toStream(os);
          builder.run();
        }

        log.info("Xuất hóa đơn thành công tại: {}", outputPath);
        return true;
      }
    } catch (Exception e) {
      log.error("Lỗi khi xuất hóa đơn PDF tại đường dẫn: {}", outputPath, e);
      return false;
    }
  }
}