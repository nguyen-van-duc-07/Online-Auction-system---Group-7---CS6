package com.auction.client;

/**
 * Lớp khởi chạy (Launcher) để chạy ứng dụng JavaFX từ fat JAR.
 * <p>
 * JavaFX yêu cầu lớp Main phải kế thừa {@link javafx.application.Application},
 * nhưng khi đóng gói fat JAR (uber JAR), Java Module System sẽ báo lỗi nếu
 * main class là Application subclass. Lớp này hoạt động như một "trampoline"
 * để gọi {@link Main#main(String[])} một cách gián tiếp, giúp bypass hạn chế đó.
 * </p>
 */
public class Launcher {
  /**
   * Điểm vào chính khi chạy ứng dụng bằng {@code java -jar}.
   *
   * @param args các tham số dòng lệnh
   */
  public static void main(String[] args) {
    Main.main(args);
  }
}
