package config;

/**
 * Hằng số nghiệp vụ đấu giá (anti-sniping, v.v.).
 */
public final class AuctionConfig {

  private AuctionConfig() {}

  /** Còn ít hơn số phút này thì kích hoạt gia hạn. */
  public static final long ANTI_SNIPE_THRESHOLD_MINUTES = 3;

  /** Gia hạn tối thiểu tính từ thời điểm hoạt động (now), không cộng dồn từ end_time cũ. */
  public static final long ANTI_SNIPE_EXTENSION_MINUTES = 3;

  public static final long PAYMENT_DURATION_DAYS = 7;
}
