package repository.debug;

import java.math.BigDecimal;

public class Format {
  public static String fmt(BigDecimal value) {
    return String.format("%,.0f đ", value);
  }
}
