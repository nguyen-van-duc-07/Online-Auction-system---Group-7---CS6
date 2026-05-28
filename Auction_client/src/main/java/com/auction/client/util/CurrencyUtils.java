package com.auction.client.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public final class CurrencyUtils {

  // 1. Khởi tạo một hằng số duy nhất, dùng chung cho toàn bộ App
  private static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("#,###");
  private static final BigDecimal ONE_MILLION = new BigDecimal("1000000");
  private static final BigDecimal ONE_THOUSAND = new BigDecimal("1000");

  // Khóa Constructor lại để không ai có thể "new" Class tiện ích này
  private CurrencyUtils() {}

  /**
   * Định dạng tiền tệ đầy đủ (Ví dụ: 42,000,000 VNĐ)
   */
  public static String formatVnd(BigDecimal amount) {
    if (amount == null) return "0 VNĐ";
    return CURRENCY_FORMATTER.format(amount) + " VNĐ";
  }
  public static String formatD(BigDecimal amount) {
    if (amount == null) return "0 đ";
    return CURRENCY_FORMATTER.format(amount) + " đ";
  }

  /**
   * Định dạng viết tắt cho các nút bấm (Ví dụ: 1.5M, 100k)
   */
  public static String formatShortAmount(BigDecimal amount) {
    if (amount == null) return "0";

    if (amount.compareTo(ONE_MILLION) >= 0) {
      BigDecimal millions = amount.divide(ONE_MILLION, 2, java.math.RoundingMode.HALF_UP);
      return millions.stripTrailingZeros().toPlainString() + "M";
    } else if (amount.compareTo(ONE_THOUSAND) >= 0) {
      BigDecimal thousands = amount.divide(ONE_THOUSAND, 1, java.math.RoundingMode.HALF_UP);
      return thousands.stripTrailingZeros().toPlainString() + "k";
    } else {
      return amount.stripTrailingZeros().toPlainString();
    }
  }
}