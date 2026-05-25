package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.request.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller trung tâm điều phối toàn bộ layout chính sau khi đăng nhập.
 *
 * <p>Quản lý thanh ngang trên (top bar), sidebar trái với 2 button cố định
 * (Quản lý hàng giao bán + Kết quả đấu giá) và vùng button động,
 * cùng ScrollPane trung tâm để load nội dung theo ngữ cảnh.</p>
 */
public class MainLayoutController implements Initializable, Controller {

  private static final Logger log = LoggerFactory.getLogger(MainLayoutController.class);

  /** Biến static lưu trữ Controller hiện tại. */
  private static MainLayoutController instance;

  /**
   * Lấy instance hiện tại của MainLayoutController.
   * @return đối tượng MainLayoutController đang active
   */
  public static MainLayoutController getInstance() {
    return instance;
  }

  // ========================== TRẠNG THÁI ==========================

  /** Ngữ cảnh hiện tại: "home", "seller", "result" */
  private String currentContext = "home";

  private HomeController homeController;
  private SellerHomeController sellerHomeController;
  private ResultController resultController;

  // ========================== FXML BINDINGS ==========================

  @FXML private ScrollPane mainContent;
  @FXML private FlowPane feedContainer;
  @FXML private VBox sideBar;
  @FXML private Label accountNameLabel;
  @FXML private Label notificationBadge;
  @FXML private TextField searchField;
  @FXML private Button homeButton;
  @FXML private Button sellerHomeButton;
  @FXML private Button resultButton;
  @FXML private VBox floatingIcon;
  @FXML private Label remainingLabel;

  @FXML private Button functionButton1;
  @FXML private Button functionButton2;
  @FXML private Button functionButton3;
  @FXML private Label lblStatusHeader;

  // ========================== STYLE CONSTANTS ==========================

  /** Style cho button cố định khi đang được chọn (active) */
  private static final String STYLE_BTN_ACTIVE =
      "-fx-background-color: white; -fx-text-fill: #1e7d32; -fx-font-weight: bold; "
      + "-fx-font-size: 14px; -fx-padding: 15; -fx-background-radius: 12; -fx-cursor: hand;";

  /** Style cho button cố định khi không được chọn (inactive) */
  private static final String STYLE_BTN_INACTIVE =
      "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-font-weight: bold; "
      + "-fx-font-size: 14px; -fx-padding: 15; -fx-background-radius: 12; -fx-cursor: hand;";

  /** Style cho button động trong sidebar (viền trắng mờ) */
  private static final String STYLE_DYNAMIC_BTN =
      "-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.3); "
      + "-fx-border-radius: 10; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; "
      + "-fx-padding: 12 15; -fx-cursor: hand;";

  /** Style cho button động đặc biệt (nền trắng mờ, giống button cố định inactive) */
  public static final String STYLE_DYNAMIC_BTN_SPECIAL =
      "-fx-background-color: transparent; -fx-text-fill: #f1c40f; "
          + "-fx-alignment: center-left; -fx-font-weight: bold; -fx-font-size: 14px; "
          + "-fx-padding: 15; -fx-background-radius: 12; -fx-cursor: hand; ";

  private static final String STYLE_FUNC_BTN_ACTIVE =
      "-fx-background-color: rgba(255,255,255,0.35); -fx-border-color: white; "
      + "-fx-border-radius: 10; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; "
      + "-fx-padding: 12 15; -fx-cursor: hand; -fx-font-weight: bold;";

  private static final String STYLE_FUNC_BTN_INACTIVE =
      "-fx-background-color: transparent; -fx-border-color: rgba(255,255,255,0.3); "
      + "-fx-border-radius: 10; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; "
      + "-fx-padding: 12 15; -fx-cursor: hand;";

  // ========================== KHỞI TẠO ==========================

  /**
   * Khởi tạo khi MainLayout được load (ngay sau khi login thành công).
   * Hiển thị tên user, load sidebar "home", và request danh sách đấu giá.
   */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;

    // Load số thông báo chưa đọc
    loadUnreadCount();

    // Hiện Label chào user
    String realName = SessionManager.currentUser.getAccountName();
    String phoneNumber = SessionManager.currentUser.getPhoneNumber();
    if (realName != null) {
      accountNameLabel.setText("Chào, " + realName);
    } else if (phoneNumber != null) {
      accountNameLabel.setText("Chào, " + phoneNumber);
    } else {
      accountNameLabel.setText("N/A");
    }

    // Hiển thị số dư lần đầu tiên khi vừa load màn hình (Tránh việc nhãn bị trống)
    if (SessionManager.getCurrentBalance() != null) {
      java.text.NumberFormat format = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
      remainingLabel.setText("Số dư: " + format.format(SessionManager.getCurrentBalance()) + " VNĐ");
    }

    //  Đăng ký Listener: Từ nay về sau, hễ SessionManager có số mới là UI tự update
    SessionManager.balanceProperty().addListener((observable, oldBalance, newBalance) -> {
      Platform.runLater(() -> {
        if (remainingLabel != null && newBalance != null) {
          java.text.NumberFormat format = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
          remainingLabel.setText("Số dư: " + format.format(newBalance) + " VNĐ");
        }
      });
    });

    // Khởi tạo các sub-controllers
    this.homeController = new HomeController(this);
    this.sellerHomeController = new SellerHomeController(this);
    this.resultController = new ResultController(this);

    // Load sidebar dạng "home"
    configureFunctionButtons("home");

    // Request danh sách đấu giá đang diễn ra
    ServerConnection.sendData(new GetActiveAuctionsRequestDTO());
    // Thêm search listener
    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
      if ("home".equals(currentContext) && homeController != null) {
        homeController.filterAuctions(newValue.trim().toLowerCase());
      }
    });
  }

  /**
   * Cập nhật style cho 2 button cố định (Quản lý hàng / Kết quả) theo context.
   * @param active "home" | "seller" | "result"
   */
  private void resetFixedButtonStyles(String active) {
    if ("seller".equals(active)) {
      homeButton.setStyle(STYLE_BTN_INACTIVE);
      sellerHomeButton.setStyle(STYLE_BTN_ACTIVE);
      resultButton.setStyle(STYLE_BTN_INACTIVE);
    } else if ("result".equals(active)) {
      homeButton.setStyle(STYLE_BTN_INACTIVE);
      sellerHomeButton.setStyle(STYLE_BTN_INACTIVE);
      resultButton.setStyle(STYLE_BTN_ACTIVE);
    } else {
      homeButton.setStyle(STYLE_BTN_ACTIVE);
      sellerHomeButton.setStyle(STYLE_BTN_INACTIVE);
      resultButton.setStyle(STYLE_BTN_INACTIVE);
    }
  }

  public void resetFunctionButtonStyles(int activeIndex) {
    functionButton1.setStyle(STYLE_FUNC_BTN_INACTIVE);
    functionButton2.setStyle(STYLE_FUNC_BTN_INACTIVE);
    functionButton3.setStyle(STYLE_FUNC_BTN_INACTIVE);

    if (activeIndex == 1) {
      functionButton1.setStyle(STYLE_FUNC_BTN_ACTIVE);
    } else if (activeIndex == 2) {
      functionButton2.setStyle(STYLE_FUNC_BTN_ACTIVE);
    } else if (activeIndex == 3) {
      functionButton3.setStyle(STYLE_FUNC_BTN_ACTIVE);
    }
  }

  public void configureFunctionButtons(String context) {
    this.currentContext = context;
    resetFixedButtonStyles(context);

    if ("home".equals(context)) {

      sideBar.setStyle("-fx-background-color: linear-gradient(to bottom, #1e7d32, #009900)");

      lblStatusHeader.setText("TRẠNG THÁI PHIÊN");
      functionButton1.setText("🟢 Đang diễn ra");
      functionButton1.setOnAction(e -> {
        resetFunctionButtonStyles(1);
        if (homeController != null) homeController.handleGetActiveAuctions();
      });
      functionButton2.setText("🕒 Sắp diễn ra");
      functionButton2.setOnAction(e -> {
        resetFunctionButtonStyles(2);
        if (homeController != null) homeController.handleGetWaitingAuctions();
      });
      functionButton3.setText("🔴 Đã kết thúc");
      functionButton3.setOnAction(e -> {
        resetFunctionButtonStyles(3);
        if (homeController != null) homeController.handleGetClosedAuctions();
      });
      resetFunctionButtonStyles(1);
    } else if ("seller".equals(context)) {

      sideBar.setStyle("-fx-background-color: linear-gradient(to bottom, #1A73E8, #009900)");

      lblStatusHeader.setText("QUẢN LÝ ĐƠN HÀNG");
      functionButton1.setText("📦 Đơn hàng chờ giao");
      functionButton1.setOnAction(e -> {
        resetFunctionButtonStyles(1);
        if (sellerHomeController != null) sellerHomeController.handleGetPendingOrders();
      });
      functionButton2.setText("✅ Giao dịch thành công");
      functionButton2.setOnAction(e -> {
        resetFunctionButtonStyles(2);
        if (sellerHomeController != null) sellerHomeController.handleGetCompletedOrders();
      });
      functionButton3.setText("❌ Phiên lỗi / Huỷ đơn");
      functionButton3.setOnAction(e -> {
        resetFunctionButtonStyles(3);
        if (sellerHomeController != null) sellerHomeController.handleGetCanceledOrders();
      });
      resetFunctionButtonStyles(0);
    } else if ("result".equals(context)) {

      sideBar.setStyle("-fx-background-color: linear-gradient(to bottom, #1e7d32, #009900)");

      lblStatusHeader.setText("ĐƠN HÀNG CỦA TÔI");
      functionButton1.setText("🕒 Chờ xác nhận");
      functionButton1.setOnAction(e -> {
        resetFunctionButtonStyles(1);
        if (resultController != null) resultController.handleGetPendingOrders();
      });
      functionButton2.setText("✅ Đã hoàn tất");
      functionButton2.setOnAction(e -> {
        resetFunctionButtonStyles(2);
        if (resultController != null) resultController.handleGetCompletedOrders();
      });
      functionButton3.setText("❌ Đã huỷ");
      functionButton3.setOnAction(e -> {
        resetFunctionButtonStyles(3);
        if (resultController != null) resultController.handleGetCancelledOrders();
      });
      resetFunctionButtonStyles(1);
    }
  }

  // ====================================================================
  //                    NẠP NỘI DUNG VÀO SCROLLPANE
  // ====================================================================

  // ====================================================================
  //                         THÔNG BÁO
  // ====================================================================

  /**
   * Nạp file FXML bất kì và thay thế toàn bộ nội dung ScrollPane.
   */
  public void loadComponent(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      javafx.scene.Parent newNode = loader.load();

      mainContent.setContent(newNode);
      mainContent.setFitToHeight(true);
      mainContent.setFitToWidth(true);
    } catch (IOException e) {
      log.error("Failed to load FXML component from path: {}", fxmlPath, e);
    }
  }

  private void loadUnreadCount() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetNotificationsRequestDTO(userId));
  }

  /**
   * Cập nhật badge thông báo (chấm đỏ) trên nút chuông.
   */
  public void updateNotificationBadge(int unreadCount) {
    Platform.runLater(() -> {
      if (unreadCount > 0) {
        notificationBadge.setVisible(true);
        notificationBadge.setText("");
      } else {
        notificationBadge.setVisible(false);
      }
    });
  }

  /**
   * Bật badge thông báo khi có thông báo mới.
   */
  public void incrementNotificationBadge() {
    Platform.runLater(() -> notificationBadge.setVisible(true));
  }

  // ====================================================================
  //                        ĐIỀU HƯỚNG (NAVIGATION)
  // ====================================================================

  /**
   * Quay về trang chủ (Home Feed): load sidebar "home" + request auctions.
   * Được gọi khi ấn logo "ĐẤU GIÁ 88".
   */
  @FXML
  public void gotoHomeFeed() {
    configureFunctionButtons("home");

    // Reset ScrollPane về FlowPane feedContainer
    mainContent.setContent(feedContainer);
    mainContent.setFitToWidth(true);
    mainContent.setFitToHeight(false);

    // Request danh sách đấu giá mới nhất
    ServerConnection.sendData(new GetActiveAuctionsRequestDTO());
  }

  /**
   * Chuyển sang quản lý hàng giao bán (Seller Home).
   * Gửi request kiểm tra hồ sơ người bán trước khi hiển thị.
   */
  @FXML
  public void gotoSellerHome() {
    String userId = SessionManager.currentUser.getId();
    CheckingSellerProfileRequestDTO request = new CheckingSellerProfileRequestDTO(userId);
    ServerConnection.sendData(request);
  }

  /**
   * Hiển thị trang Seller Home sau khi hồ sơ người bán đã được xác nhận.
   * Được gọi bởi ResponseHandler sau khi server trả về kết quả check profile.
   */
  public void showSellerHome() {
    Platform.runLater(() -> {
      configureFunctionButtons("seller");

      // Reset ScrollPane
      mainContent.setContent(feedContainer);
      mainContent.setFitToWidth(true);
      mainContent.setFitToHeight(false);

      // Request danh sách phiên đấu giá của seller
      String sellerId = SessionManager.getCurrentUser().getId();
      ServerConnection.sendData(new GetAuctionsBySellerRequestDTO(sellerId));
    });
  }

  /**
   * Chuyển sang trang Kết quả đấu giá.
   * Load sidebar "result" + request danh sách đơn hàng chờ.
   */
  @FXML
  public void gotoResult() {
    configureFunctionButtons("result");

    // Request danh sách đơn hàng pending của buyer
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetPendingOrdersOfBuyerRequestDTO(userId));
  }

  /** Mở trang thông tin cá nhân trong ScrollPane. */
  @FXML
  public void gotoProfile() {
    loadComponent("/com/auction/client/User/Profile.fxml");
  }

  /** Mở trang ví người dùng trong ScrollPane. */
  @FXML
  public void gotoWallet() {
    loadComponent("/com/auction/client/User/Wallet/Wallet.fxml");
  }

  /** Đăng xuất: hiện hộp thoại xác nhận, gửi request logout, quay về Login. */
  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Xác nhận đăng xuất",
        "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(response -> {
      if (response == ButtonType.OK) {
        LogoutRequestDTO logoutRequestDTO = new LogoutRequestDTO();
        logoutRequestDTO.setUserId(SessionManager.currentUser.getId());
        ServerConnection.sendData(logoutRequestDTO);
        SessionManager.setCurrentUser(null);
        ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");
      }
    });
  }

  /** Mở cửa sổ thông báo (sub window). */
  @FXML
  public void gotoNotifications() {
    ScreenController.createSubWindow("Bidder/Notifications.fxml", "Thông báo");
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetNotificationsRequestDTO(userId));
  }

  /** Mở trang đăng bán sản phẩm trong ScrollPane. */
  public void gotoUploadItem() {
    loadComponent("/com/auction/client/Seller/UploadItem.fxml");
  }

  // ====================================================================
  //                          GETTERS
  // ====================================================================

  public HomeController getHomeController() {
    return homeController;
  }

  public SellerHomeController getSellerHomeController() {
    return sellerHomeController;
  }

  public ResultController getResultController() {
    return resultController;
  }

  public FlowPane getFeedContainer() {
    return feedContainer;
  }

  public ScrollPane getMainContent() {
    return mainContent;
  }

  public Label getAccountNameLabel() {
    return accountNameLabel;
  }

  public void setCurrentContext(String context) {
    this.currentContext = context;
  }

  /** Lấy ngữ cảnh hiện tại ("home", "seller", "result"). */
  public String getCurrentContext() {
    return currentContext;
  }
}
