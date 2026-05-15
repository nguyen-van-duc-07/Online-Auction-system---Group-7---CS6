package scheduler;

import service.OrderService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrderExpiryScheduler {

  private final OrderService orderService = new OrderService();

  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor();

  public void start() {
    scheduler.scheduleAtFixedRate(
        this::checkExpiredOrders,
        0,
        1,
        TimeUnit.HOURS
    );
  }

  private void checkExpiredOrders() {
    try {
      System.out.println("[ORDER EXPIRY] Đang kiểm tra các order hết hạn...");
      orderService.cancelExpiredOrders();
    } catch (Exception e) {
      System.out.println("[ORDER EXPIRY] LỖI: " + e.getMessage());
      e.printStackTrace();
    }
  }
}