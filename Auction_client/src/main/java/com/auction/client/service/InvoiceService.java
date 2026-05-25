package com.auction.client.service;

import com.auction.shared.model.transaction.PrizedTransaction;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceService {
  private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
  public void exportInvoiceToPdf(PrizedTransaction transaction, String outputPath) {
    try {
      // 1. Đọc file template HTML từ thư mục resources
      ClassLoader classLoader = getClass().getClassLoader();
      try (InputStream inputStream = classLoader.getResourceAsStream("invoice_template.html")) {
        if (inputStream == null) {
          throw new IllegalArgumentException("Không tìm thấy file template!");
        }

        String htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        // 2. Trộn dữ liệu từ object PrizedTransaction vào HTML (Thay thế placeholder)
        htmlContent = htmlContent
            .replace("{{auctionId}}", transaction.getAuctionId())
            .replace("{{fromId}}", transaction.getFromId())
            .replace("{{toId}}", transaction.getToId())
            .replace("{{itemId}}", transaction.getItemId())
            .replace("{{finalPrice}}", transaction.getFinalPrice().toString());

        // 3. Tiến hành render chuỗi HTML hoàn chỉnh thành file PDF
        try (OutputStream os = new FileOutputStream(outputPath)) {
          PdfRendererBuilder builder = new PdfRendererBuilder();
          builder.useFastMode();
          // Truyền chuỗi HTML và một URL cơ sở (để tải ảnh/css nếu có)
          builder.withHtmlContent(htmlContent, "/");
          builder.toStream(os);
          builder.run();
        }

        log.info("Xuất hóa đơn thành công tại: {}", outputPath);
      }
    } catch (Exception e) {
      log.error("Lỗi khi xuất hóa đơn PDF tại đường dẫn: {}", outputPath, e);
    }
  }
}