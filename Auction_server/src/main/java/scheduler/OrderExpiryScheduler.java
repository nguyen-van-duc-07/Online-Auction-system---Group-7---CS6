package scheduler;

import service.OrderService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderExpiryScheduler {
  private static final Logger log = LoggerFactory.getLogger(OrderExpiryScheduler.class);

  private final OrderService orderService;

  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor();

  /**
   * Constructor mặc định cho Production.
   */
  public OrderExpiryScheduler() {
    this(OrderService.getInstance());
  }

  /**
   * Constructor nhận tham số phục vụ cho Unit Test.
   */
  public OrderExpiryScheduler(OrderService orderService) {
    this.orderService = orderService;
  }

  public void start() {
    scheduler.scheduleAtFixedRate(
        this::checkExpiredOrders,
        0,
        5,
        TimeUnit.SECONDS
    );
  }
  public void stop() {
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Package-private để hỗ trợ Unit Test trực tiếp.
   */
  void checkExpiredOrders() {
    try {
      orderService.cancelExpiredOrders();
    } catch (Exception e) {
      log.error("[ORDER EXPIRY] Lỗi nghiêm trọng khi kiểm tra order hết hạn", e);
    }
  }
}