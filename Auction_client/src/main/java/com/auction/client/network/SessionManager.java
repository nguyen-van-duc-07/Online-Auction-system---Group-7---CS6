package com.auction.client.network;

import com.auction.client.screenhandler.Controller;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.item.ItemDTO;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.response.AuctionResponseDTO;
import com.auction.shared.response.GetBalanceResponseDTO;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Lớp quản lý phiên làm việc (Session) của người dùng hiện tại ở phía Client.
 *
 * <p>Lớp sử dụng các biến {@code static} để lưu trữ thông tin người dùng xuyên suốt
 * vòng đời của ứng dụng JavaFX, giúp các màn hình khác nhau (như Profile, Wallet)
 * có thể truy xuất dữ liệu một cách tập trung mà không cần truyền tham số qua lại.</p>
 */
public class SessionManager {
  private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

  /**
   * Đối tượng lưu giữ thông tin của người dùng đang đăng nhập vào hệ thống.
   */
  public static UserDTO currentUser;

  /**
   * Đối tượng lưu giữ Id của phiên đấu giá mà người dùng vừa ấn vào.
   */
  public static String currentAuctionId;

  /**
   * ĐỐi tượng lưu giữ thông tin của đơn hàng hiện tại.
   */
  public static String currentOrderId;

  /**
   * Đối tượng lưu giữ thông tin controller của màn hình trước.
   */
  public static Controller previousScreen;

  /**
   * Khai báo kênh lắng nghe biến động số dư.
   */
  public static final ObjectProperty<BigDecimal> balanceProperty = new SimpleObjectProperty<>(BigDecimal.ZERO);

  public static void setCurrentUser(UserDTO currentUser) {
    SessionManager.currentUser = currentUser;
  }

  public static void setCurrentAuctionId(String currentAuctionId) {
    SessionManager.currentAuctionId = currentAuctionId;
  }

  public static void setCurrentOrderId(String currentOrderId) {
    SessionManager.currentOrderId = currentOrderId;
  }

  public static void setPreviousScreen(Controller previousScreen) {
    SessionManager.previousScreen = previousScreen;
  }

  public static UserDTO getCurrentUser() {
    return SessionManager.currentUser;
  }

  public static String getCurrentAuctionId() {
    return SessionManager.currentAuctionId;
  }

  public static String getCurrentOrderId() {
    return SessionManager.currentOrderId;
  }

  public static Controller getPreviousScreen() {
    return SessionManager.previousScreen;
  }

  /**
   * Hàm tập trung để cập nhật số dư từ bất kỳ đâu (Màn hình ví, luồng nhận sự kiện Socket...).
   */
  public static void updateBalance(GetBalanceResponseDTO response) {
    if (response != null && response.isSuccess()) {
      BigDecimal newBalance = response.getBalance();
      balanceProperty.set(newBalance);
    } else {
      log.warn("Cảnh báo: Gói tin lấy số dư bị lỗi hoặc null!");
    }
  }

  /**
   * Hàm tiện ích để lấy nhanh số dư hiện tại dưới dạng số khi cần tính toán ở các controller khác.
   */
  public static BigDecimal getCurrentBalance() {
    return balanceProperty.get();
  }

  public static ObjectProperty<BigDecimal> balanceProperty() {
    return balanceProperty;
  }
}