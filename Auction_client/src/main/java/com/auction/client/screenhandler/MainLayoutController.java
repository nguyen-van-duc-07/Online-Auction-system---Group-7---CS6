package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.enums.OrderStatus;
import com.auction.shared.util.CurrencyUtils;
import com.auction.shared.enums.AuctionStatus;
import com.auction.shared.enums.ItemType;
import com.auction.shared.request.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
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
public class MainLayoutController implements Initializable {

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
  @FXML private Label remainingLabel;

  @FXML private Button functionButton1;
  @FXML private Button functionButton2;
  @FXML private Button functionButton3;
  @FXML private Label lblStatusHeader;

  // Filter bar
  @FXML private HBox filterBar;
  @FXML private MenuButton categoryFilter;
  @FXML private Label filterLabel;
  @FXML private Label placeholderLabel;

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
   * Khởi tạo giao diện chính sau khi đăng nhập thành công.
   * Hiển thị tên tài khoản, số dư hiện tại, cài đặt sub-controller và nạp danh sách ban đầu.
   *
   * @param location vị trí đường dẫn tương đối của đối tượng gốc
   * @param resources tài nguyên sử dụng để bản địa hóa đối tượng gốc
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
      remainingLabel.setText("Số dư: " + CurrencyUtils.formatVnd(SessionManager.getCurrentBalance()));
    }

    //  Đăng ký Listener: Từ nay về sau, hễ SessionManager có số mới là UI tự update
    SessionManager.balanceProperty().addListener((observable, oldBalance, newBalance) -> {
      Platform.runLater(() -> {
        if (remainingLabel != null && newBalance != null) {
          remainingLabel.setText("Số dư: " + CurrencyUtils.formatVnd(newBalance));
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
    ServerConnection.sendData(new GetAuctionsRequestDTO(AuctionStatus.ACTIVE));
    // Thêm search listener
    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
      String query = newValue.trim().toLowerCase();
      if ("home".equals(currentContext)) {
        if (homeController != null) {
          homeController.filterAuctions(query);
        }
      } else if ("seller".equals(currentContext)) {
        if (filterBar.isVisible()) {
          if (sellerHomeController != null) {
            sellerHomeController.filterAuctions(query);
          }
        } else {
          if (resultController != null) {
            resultController.filterOrders(query);
          }
        }
      } else if ("result".equals(currentContext)) {
        if (resultController != null) {
          resultController.filterOrders(query);
        }
      }
    });
  }

  // ========================== FILTER BAR ==========================

  /**
   * Cấu hình MenuButton filter tùy theo context.
   * - "home": lọc theo ItemType (Tất cả danh mục, Thiết bị điện tử, ...)
   * - "seller": lọc theo AuctionStatus (Tất cả, Đang diễn ra, Sắp diễn ra, Đã kết thúc)
   * - "result": ẩn filter bar
   */
  /**
   * Bật/tắt hiển thị thanh filter lọc sản phẩm (HBox filterBar).
   */
  /**
   * Bật hoặc tắt hiển thị thanh lọc sản phẩm trên giao diện.
   *
   * @param visible {@code true} nếu hiển thị thanh lọc, ngược lại {@code false}
   */
  public void setFilterBarVisible(boolean visible) {
    if (filterBar != null) {
      filterBar.setVisible(visible);
      filterBar.setManaged(visible);
    }
  }

  private void configureFilterBar(String context) {
    categoryFilter.getItems().clear();

    if ("home".equals(context)) {
      setFilterBarVisible(true);
      filterLabel.setText("Phân loại:");
      categoryFilter.setText("Tất cả danh mục");

      // Item "Tất cả"
      MenuItem allItem = new MenuItem("Tất cả danh mục");
      allItem.setOnAction(e -> {
        categoryFilter.setText("Tất cả danh mục");
        if (homeController != null) homeController.filterByCategory(null);
      });
      categoryFilter.getItems().add(allItem);

      // Các ItemType
      for (ItemType type : ItemType.values()) {
        MenuItem mi = new MenuItem(type.getValue());
        mi.setOnAction(e -> {
          categoryFilter.setText(type.getValue());
          if (homeController != null) homeController.filterByCategory(type);
        });
        categoryFilter.getItems().add(mi);
      }

    } else if ("seller".equals(context)) {
      setFilterBarVisible(true);
      filterLabel.setText("Trạng thái:");
      categoryFilter.setText("Tất cả phiên");

      // Item "Tất cả"
      MenuItem allItem = new MenuItem("Tất cả phiên");
      allItem.setOnAction(e -> {
        categoryFilter.setText("Tất cả phiên");
        if (sellerHomeController != null) sellerHomeController.filterByStatus(null);
      });
      categoryFilter.getItems().add(allItem);

      // Các status
      String[] statusLabels = {"🟢 Đang diễn ra", "🕒 Sắp diễn ra", "🔴 Đã kết thúc"};
      AuctionStatus[] statusValues = {AuctionStatus.ACTIVE, AuctionStatus.WAITING, AuctionStatus.CLOSED};
      for (int i = 0; i < statusLabels.length; i++) {
        String label = statusLabels[i];
        AuctionStatus status = statusValues[i];
        MenuItem mi = new MenuItem(label);
        mi.setOnAction(e -> {
          categoryFilter.setText(label);
          if (sellerHomeController != null) sellerHomeController.filterByStatus(status);
        });
        categoryFilter.getItems().add(mi);
      }

    } else {
      // "result" — ẩn filter bar
      setFilterBarVisible(false);
    }
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

  /**
   * Đặt lại kiểu dáng các nút chức năng động trong sidebar.
   *
   * @param activeIndex chỉ số nút chức năng đang kích hoạt (1, 2 hoặc 3)
   */
  private void resetFunctionButtonStyles(int activeIndex) {
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

  /**
   * Cấu hình các nút chức năng trong sidebar theo ngữ cảnh tương ứng.
   *
   * @param context ngữ cảnh hiển thị hiện tại ("home", "seller", hoặc "result")
   */
  private void configureFunctionButtons(String context) {
    this.currentContext = context;
    resetFixedButtonStyles(context);
    configureFilterBar(context);

    if ("home".equals(context)) {

      sideBar.setStyle("-fx-background-color: linear-gradient(to bottom, #1e7d32, #009900)");

      lblStatusHeader.setText("TRẠNG THÁI PHIÊN");
      functionButton1.setText("🟢 Đang diễn ra");
      functionButton1.setOnAction(e -> {
        resetFunctionButtonStyles(1);
        searchField.clear();
        if (homeController != null) homeController.handleGetActiveAuctions();
      });
      functionButton2.setText("🕒 Sắp diễn ra");
      functionButton2.setOnAction(e -> {
        resetFunctionButtonStyles(2);
        searchField.clear();
        if (homeController != null) homeController.handleGetWaitingAuctions();
      });
      functionButton3.setText("🔴 Đã kết thúc");
      functionButton3.setOnAction(e -> {
        resetFunctionButtonStyles(3);
        searchField.clear();
        if (homeController != null) homeController.handleGetClosedAuctions();
      });
      resetFunctionButtonStyles(1);
    } else if ("seller".equals(context)) {

      sideBar.setStyle("-fx-background-color: linear-gradient(to bottom, #1A73E8, #009900)");

      lblStatusHeader.setText("QUẢN LÝ ĐƠN HÀNG");
      functionButton1.setText("📦 Đơn hàng chờ giao");
      functionButton1.setOnAction(e -> {
        resetFunctionButtonStyles(1);
        filterBar.setVisible(false);
        filterBar.setManaged(false);
        searchField.clear();
        if (sellerHomeController != null) sellerHomeController.handleGetPendingOrders();
      });
      functionButton2.setText("✅ Giao dịch thành công");
      functionButton2.setOnAction(e -> {
        resetFunctionButtonStyles(2);
        filterBar.setVisible(false);
        filterBar.setManaged(false);
        searchField.clear();
        if (sellerHomeController != null) sellerHomeController.handleGetConfirmedOrders();
      });
      functionButton3.setText("❌ Phiên lỗi / Huỷ đơn");
      functionButton3.setOnAction(e -> {
        resetFunctionButtonStyles(3);
        filterBar.setVisible(false);
        filterBar.setManaged(false);
        searchField.clear();
        if (sellerHomeController != null) sellerHomeController.handleGetCanceledOrders();
      });
      resetFunctionButtonStyles(0);
    } else if ("result".equals(context)) {

      sideBar.setStyle("-fx-background-color: linear-gradient(to bottom, #1e7d32, #009900)");

      lblStatusHeader.setText("ĐƠN HÀNG CỦA TÔI");
      functionButton1.setText("🕒 Chờ xác nhận");
      functionButton1.setOnAction(e -> {
        resetFunctionButtonStyles(1);
        searchField.clear();
        if (resultController != null) resultController.handleGetPendingOrders();
      });
      functionButton2.setText("✅ Đã hoàn tất");
      functionButton2.setOnAction(e -> {
        resetFunctionButtonStyles(2);
        searchField.clear();
        if (resultController != null) resultController.handleGetConfirmedOrders();
      });
      functionButton3.setText("❌ Đã huỷ");
      functionButton3.setOnAction(e -> {
        resetFunctionButtonStyles(3);
        searchField.clear();
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
   * Hiển thị dòng thông báo tạm thời khi danh sách trống.
   *
   * @param text nội dung thông báo hiển thị
   */
  public void showPlaceholder(String text) {
    if (placeholderLabel != null) {
      placeholderLabel.setText(text);
      placeholderLabel.setVisible(true);
    }
  }

  /**
   * Ẩn dòng thông báo tạm thời khi có dữ liệu hiển thị.
   */
  public void hidePlaceholder() {
    if (placeholderLabel != null) {
      placeholderLabel.setVisible(false);
    }
  }

  /**
   * Nạp tệp FXML bất kỳ và thay thế toàn bộ nội dung hiển thị trong ScrollPane chính.
   *
   * @param fxmlPath đường dẫn tệp FXML cần nạp
   */
  public void loadComponent(String fxmlPath) {
    try {
      setFilterBarVisible(false); // Tự động ẩn thanh lọc khi chuyển sang trang khác
      hidePlaceholder(); // Ẩn placeholder khi nạp màn hình FXML khác
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      javafx.scene.Parent newNode = loader.load();

      mainContent.setContent(newNode);
      mainContent.setFitToHeight(true);
      mainContent.setFitToWidth(true);
    } catch (IOException e) {
      log.error("Không thể load thông tin trong file .fxml: {}", fxmlPath, e);
    }
  }

  private void loadUnreadCount() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetNotificationsRequestDTO(userId));
  }

  /**
   * Cập nhật badge hiển thị số lượng thông báo chưa đọc trên biểu tượng chuông.
   *
   * @param unreadCount số lượng thông báo chưa đọc
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
   * Bật hiển thị badge chấm đỏ khi có thông báo mới được gửi tới.
   */
  public void incrementNotificationBadge() {
    Platform.runLater(() -> notificationBadge.setVisible(true));
  }

  // ====================================================================
  //                        ĐIỀU HƯỚNG (NAVIGATION)
  // ====================================================================

  /**
   * Quay về trang chủ xem danh sách phiên đấu giá (Home Feed).
   */
  @FXML
  public void gotoHomeFeed() {
    searchField.clear();
    configureFunctionButtons("home");

    // Reset ScrollPane về FlowPane feedContainer
    mainContent.setContent(feedContainer);
    mainContent.setFitToWidth(true);
    mainContent.setFitToHeight(false);

    // Reset filter
    categoryFilter.setText("Tất cả danh mục");

    hidePlaceholder();

    // Request danh sách đấu giá mới nhất
    ServerConnection.sendData(new GetAuctionsRequestDTO(AuctionStatus.ACTIVE));
  }

  /**
   * Gửi yêu cầu kiểm tra hồ sơ người bán trước khi chuyển sang Seller Home.
   */
  @FXML
  public void gotoSellerHome() {
    String userId = SessionManager.currentUser.getId();
    CheckingSellerProfileRequestDTO request = new CheckingSellerProfileRequestDTO(userId);
    ServerConnection.sendData(request);
  }

  /**
   * Hiển thị trang quản lý bán hàng (Seller Home) của người bán.
   */
  public void showSellerHome() {
    Platform.runLater(() -> {
      searchField.clear();
      configureFunctionButtons("seller");

      // Reset ScrollPane
      mainContent.setContent(feedContainer);
      mainContent.setFitToWidth(true);
      mainContent.setFitToHeight(false);

      hidePlaceholder();

      // Request danh sách phiên đấu giá của seller
      String userId = SessionManager.getCurrentUser().getId();
      ServerConnection.sendData(new GetAuctionsBySellerRequestDTO(userId));
    });
  }

  /**
   * Chuyển hướng người dùng sang trang xem kết quả đấu giá các đơn hàng.
   */
  @FXML
  public void gotoResult() {
    searchField.clear();
    configureFunctionButtons("result");

    hidePlaceholder();

    // Request danh sách đơn hàng pending của buyer
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetOrdersOfBuyerRequestDTO(userId, OrderStatus.PENDING));
  }

  /**
   * Hiển thị màn hình xem và cập nhật thông tin cá nhân.
   */
  @FXML
  public void gotoProfile() {
    loadComponent("/com/auction/client/User/Profile.fxml");
  }

  /**
   * Hiển thị giao diện ví điện tử cá nhân.
   */
  @FXML
  public void gotoWallet() {
    loadComponent("/com/auction/client/User/Wallet/Wallet.fxml");
  }

  /**
   * Thực hiện các thủ tục đăng xuất tài khoản và chuyển hướng quay lại màn hình Login.
   */
  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Xác nhận đăng xuất",
        "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(response -> {
      if (response == ButtonType.OK) {
        LogoutRequestDTO logoutRequestDTO = new LogoutRequestDTO();
        logoutRequestDTO.setUserId(SessionManager.currentUser.getId());
        ServerConnection.sendData(logoutRequestDTO);
        SessionManager.setCurrentUser(null);
        ScreenController.clearHistory(); // Xóa lịch sử màn hình để tránh giữ tham chiếu các controller cũ
        ScreenController.switchScreen("User/Login.fxml", "Đăng nhập");
      }
    });
  }

  /**
   * Hiển thị cửa sổ con danh sách các thông báo của người dùng.
   */
  @FXML
  public void gotoNotifications() {
    ScreenController.createSubWindow("User/Notifications.fxml", "Thông báo");
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetNotificationsRequestDTO(userId));
  }

  /**
   * Hiển thị cửa sổ con hỗ trợ đổi mật khẩu.
   */
  @FXML
  public void gotoChangePassword() {
    ScreenController.createSubWindow("User/ChangePasswordForm.fxml", "Đổi mật khẩu");
  }

  /**
   * Chuyển hướng giao diện chính sang form đăng bán sản phẩm mới.
   */
  public void gotoUploadItem() {
    loadComponent("/com/auction/client/Seller/UploadItem.fxml");
  }

  // ====================================================================
  //                          GETTERS
  // ====================================================================

  /**
   * Lấy bộ điều khiển trang chủ.
   *
   * @return bộ điều khiển HomeController
   */
  public HomeController getHomeController() {
    return homeController;
  }

  /**
   * Lấy bộ điều khiển trang chủ của người bán.
   *
   * @return bộ điều khiển SellerHomeController
   */
  public SellerHomeController getSellerHomeController() {
    return sellerHomeController;
  }

  /**
   * Lấy bộ điều khiển trang kết quả đơn hàng.
   *
   * @return bộ điều khiển ResultController
   */
  public ResultController getResultController() {
    return resultController;
  }

  /**
   * Lấy FlowPane chứa danh sách card hiển thị sản phẩm đấu giá.
   *
   * @return đối tượng FlowPane
   */
  public FlowPane getFeedContainer() {
    return feedContainer;
  }

  /**
   * Lấy ScrollPane hiển thị nội dung chính.
   *
   * @return đối tượng ScrollPane mainContent
   */
  public ScrollPane getMainContent() {
    return mainContent;
  }

  /**
   * Lấy nhãn hiển thị tên chào người dùng.
   *
   * @return nhãn Label accountNameLabel
   */
  public Label getAccountNameLabel() {
    return accountNameLabel;
  }

  /**
   * Thiết lập ngữ cảnh hiển thị hiện tại của sidebar.
   *
   * @param context tên ngữ cảnh ("home", "seller", hoặc "result")
   */
  public void setCurrentContext(String context) {
    this.currentContext = context;
  }

  /**
   * Lấy tên ngữ cảnh hiển thị hiện tại.
   *
   * @return tên ngữ cảnh dưới dạng chuỗi
   */
  public String getCurrentContext() {
    return currentContext;
  }

  /**
   * Lấy ô nhập liệu tìm kiếm sản phẩm.
   *
   * @return đối tượng TextField searchField
   */
  public TextField getSearchField() {
    return searchField;
  }
}
