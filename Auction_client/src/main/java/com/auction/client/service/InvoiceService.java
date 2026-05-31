package com.auction.client.service;

import com.auction.shared.model.order.Order;
import com.auction.shared.util.CurrencyUtils;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Dịch vụ xuất hóa đơn chi tiết cho đơn hàng thắng cuộc dưới dạng tệp PDF.
 * <p>
 * Lớp này sử dụng thư viện OpenHTMLtoPDF để kết xuất mẫu hóa đơn HTML cùng dữ liệu đơn hàng
 * thành định dạng tài liệu PDF chất lượng cao, có hỗ trợ hiển thị phông chữ tiếng Việt.
 * </p>
 */
public class InvoiceService {
  private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

  /**
   * Phương thức xuất hóa đơn chi tiết ra file PDF với đầy đủ dữ liệu người mua và sản phẩm.
   */
  public boolean exportInvoiceToPdf(
      Order order,
      BigDecimal shippingFee,
      BigDecimal totalAmount,
      String outputPath
  ) {
    try {
      // 1. Đọc file template HTML từ tài nguyên
      try (InputStream inputStream = InvoiceService.class.getResourceAsStream("/invoice_template.html")) {
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
            .replace("{{auctionId}}", order.getAuctionId() != null ? order.getAuctionId() : "N/A")
            .replace("{{transactionId}}", "TXN" + System.currentTimeMillis())
            .replace("{{consigneeName}}", order.getConsigneeName() != null && !order.getConsigneeName().isEmpty() ? order.getConsigneeName() : "N/A")
            .replace("{{phoneNumber}}", order.getPhoneNumber() != null && !order.getPhoneNumber().isEmpty() ? order.getPhoneNumber() : "N/A")
            .replace("{{address}}", order.getAddress() != null && !order.getAddress().isEmpty() ? order.getAddress() : "N/A")
            .replace("{{itemId}}", order.getId() != null ? order.getId() : "N/A")
            .replace("{{itemName}}", order.getItemName() != null && !order.getItemName().isEmpty() ? order.getItemName() : "Sản phẩm đấu giá")
            .replace("{{itemPrice}}", CurrencyUtils.formatD(order.getFinalPrice()))
            .replace("{{subTotal}}", CurrencyUtils.formatD(order.getFinalPrice()))
            .replace("{{shippingFee}}", CurrencyUtils.formatD(shippingFee))
            .replace("{{totalAmount}}", CurrencyUtils.formatD(totalAmount));

        // 3. Tiến hành render chuỗi HTML hoàn chỉnh thành file PDF
        try (OutputStream os = new FileOutputStream(outputPath)) {
          PdfRendererBuilder builder = new PdfRendererBuilder();
          builder.useFastMode();

          // KHẮC PHỤC LỖI TIẾNG VIỆT: Tải font Arial.ttf từ tài nguyên classpath thông qua tệp tạm
          // Cách này tránh sử dụng trực tiếp FSSupplier (giải quyết lỗi Java Module JPMS)
          File tempFontFile = File.createTempFile("Arial", ".ttf");
          tempFontFile.deleteOnExit();
          try (InputStream is = InvoiceService.class.getResourceAsStream("/Arial.ttf");
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
