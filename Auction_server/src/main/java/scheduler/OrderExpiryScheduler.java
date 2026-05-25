package scheduler;

import service.OrderService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderExpiryScheduler {
  private static final Logger log = LoggerFactory.getLogger(OrderExpiryScheduler.class);

  private final OrderService orderService = new OrderService();

  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor();

  public void start() {
    scheduler.scheduleAtFixedRate(
        this::checkExpiredOrders,
        0,
        5,
        TimeUnit.SECONDS
    );
  }

  private void checkExpiredOrders() {
    try {
      log.info("[ORDER EXPIRY] Đang kiểm tra các order hết hạn...");
      orderService.cancelExpiredOrders();
    } catch (Exception e) {
      log.error("[ORDER EXPIRY] Lỗi nghiêm trọng khi kiểm tra order hết hạn", e);
    }
  }
}