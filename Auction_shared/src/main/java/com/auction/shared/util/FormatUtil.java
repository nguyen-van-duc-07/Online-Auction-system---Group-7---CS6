package com.auction.shared.util;

import java.math.BigDecimal;

public class FormatUtil {
  public static String fmt(BigDecimal value) {
    return String.format("%,.0f đ", value);
  }
}
