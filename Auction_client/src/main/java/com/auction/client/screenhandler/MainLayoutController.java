package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.auction.AuctionDTO;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.request.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller trung tâm điều phối toàn bộ layout chính sau khi đăng nhập.
 *
 * <p>Quản lý thanh ngang trên (top bar), sidebar trái với 2 button cố định
 * (Quản lý hàng giao bán + Kết quả đấu giá) và vùng button động,
 * cùng ScrollPane trung tâm để load nội dung theo ngữ cảnh.</p>
 */
public class MainLayoutController implements Initializable, Controller {

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

  /** Danh sách auction đang hiển thị, dùng cho cập nhật giá realtime */
  private List<AuctionDTO> currentAuctions = new ArrayList<>();

  // ========================== FXML BINDINGS ==========================

  @FXML private ScrollPane mainContent;
  @FXML private FlowPane feedContainer;
  @FXML private VBox sidebarDynamic;
  @FXML private VBox sidebar;
  @FXML private Label realNameLabel;
  @FXML private Label lblSoDu;
  @FXML private Label notificationBadge;
  @FXML private TextField searchField;
  @FXML private Button btnQuanLy;
  @FXML private Button btnKetQua;
  @FXML private VBox floatingIcon;

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
  private static final String STYLE_DYNAMIC_BTN_SPECIAL =
      "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-font-weight: bold; "
      + "-fx-font-size: 14px; -fx-padding: 15; -fx-background-radius: 12; -fx-cursor: hand;";

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
    String realName = SessionManager.currentUser.getRealName();
    String phoneNumber = SessionManager.currentUser.getPhoneNumber();
    if (realName != null) {
      realNameLabel.setText("Chào, " + realName);
    } else if (phoneNumber != null) {
      realNameLabel.setText("Chào, " + phoneNumber);
    } else {
      realNameLabel.setText("N/A");
    }

    // Load sidebar dạng "home" (Đang diễn ra, Sắp diễn ra, Đã kết thúc)
    loadHomeSidebar();
    resetFixedButtonStyles("home");

    // Request danh sách đấu giá đang diễn ra + sắp diễn ra
    ServerConnection.sendData(new GetActiveAndWaitingAuctionsRequestDTO());
  }

  // ====================================================================
  //                      SIDEBAR — LOAD BUTTON ĐỘNG
  // ====================================================================

  /**
   * Load sidebar cho ngữ cảnh "home" (Bidder).
   * Hiển thị: Label "TRẠNG THÁI PHIÊN" + 3 button trạng thái.
   */
  public void loadHomeSidebar() {
    currentContext = "home";
    sidebarDynamic.getChildren().clear();

    // Label header
    Label header = createSidebarLabel("TRẠNG THÁI PHIÊN");
    VBox.setMargin(header, new Insets(25, 0, 5, 10));

    // 3 button trạng thái phiên
    Button btnActive = createDynamicButton("🟢 Đang diễn ra",
        e -> handleGetActiveAuctions());
    Button btnWaiting = createDynamicButton("🕒 Sắp diễn ra",
        e -> handleGetWaitingAuctions());
    Button btnClosed = createDynamicButton("🔴 Đã kết thúc",
        e -> handleGetClosedAuctions());

    sidebarDynamic.getChildren().addAll(header, btnActive, btnWaiting, btnClosed);
  }

  /**
   * Load sidebar cho ngữ cảnh "seller" (Quản lý hàng giao bán).
   * Hiển thị: Button phiên đang đấu giá + Label + 3 button quản lý đơn hàng.
   */
  public void loadSellerSidebar() {
    currentContext = "seller";
    sidebarDynamic.getChildren().clear();

    // Button "Phiên đang đấu giá" (style đặc biệt)
    Button btnMyActive = createDynamicButton("⚡ Phiên đang đấu giá",
        e -> handleGetMyActiveAuctions());
    btnMyActive.setStyle(STYLE_DYNAMIC_BTN_SPECIAL);

    // Label header quản lý đơn hàng
    Label header = createSidebarLabel("QUẢN LÝ ĐƠN HÀNG");
    VBox.setMargin(header, new Insets(25, 0, 5, 10));

    // 3 button quản lý đơn hàng
    Button btnPending = createDynamicButton("📦 Đơn hàng chờ giao",
        e -> handleGetPendingOrders());
    Button btnCompleted = createDynamicButton("✅ Giao dịch thành công",
        e -> handleGetCompletedOrders());
    Button btnCanceled = createDynamicButton("❌ Phiên lỗi / Huỷ đơn",
        e -> handleGetCanceledOrders());

    sidebarDynamic.getChildren().addAll(btnMyActive, header, btnPending, btnCompleted, btnCanceled);
  }

  /**
   * Load sidebar cho ngữ cảnh "result" (Kết quả đấu giá).
   * Hiển thị: Label "ĐƠN HÀNG CỦA TÔI" + 2 button.
   */
  public void loadResultSidebar() {
    currentContext = "result";
    sidebarDynamic.getChildren().clear();

    // Label header
    Label header = createSidebarLabel("ĐƠN HÀNG CỦA TÔI");
    VBox.setMargin(header, new Insets(25, 0, 5, 10));

    // 2 button
    Button btnPending = createDynamicButton("🕒 Chờ xác nhận",
        e -> { /* TODO: implement */ });
    Button btnCompleted = createDynamicButton("🔴 Đã hoàn tất",
        e -> { /* TODO: implement */ });

    sidebarDynamic.getChildren().addAll(header, btnPending, btnCompleted);
  }

  // ====================================================================
  //                    HELPER — TẠO COMPONENT SIDEBAR
  // ====================================================================

  /**
   * Tạo Label header cho sidebar (màu xanh nhạt, font bold nhỏ).
   */
  private Label createSidebarLabel(String text) {
    Label label = new Label(text);
    label.setTextFill(Color.web("#c8e6c9"));
    label.setFont(Font.font("System", 11));
    label.setStyle("-fx-font-weight: bold;");
    return label;
  }

  /**
   * Tạo Button động cho sidebar (viền trắng mờ, font bold).
   */
  private Button createDynamicButton(String text,
      javafx.event.EventHandler<ActionEvent> handler) {
    Button btn = new Button(text);
    btn.setMaxWidth(Double.MAX_VALUE);
    btn.setMnemonicParsing(false);
    btn.setStyle(STYLE_DYNAMIC_BTN);
    btn.setFont(Font.font("System Bold", 12));
    btn.setOnAction(handler);
    return btn;
  }

  /**
   * Cập nhật style cho 2 button cố định (Quản lý hàng / Kết quả) theo context.
   * @param active "home" | "seller" | "result"
   */
  private void resetFixedButtonStyles(String active) {
    if ("seller".equals(active)) {
      btnQuanLy.setStyle(STYLE_BTN_ACTIVE);
      btnKetQua.setStyle(STYLE_BTN_INACTIVE);
    } else if ("result".equals(active)) {
      btnQuanLy.setStyle(STYLE_BTN_INACTIVE);
      btnKetQua.setStyle(STYLE_BTN_ACTIVE);
    } else {
      // "home" — cả 2 đều inactive
      btnQuanLy.setStyle(STYLE_BTN_INACTIVE);
      btnKetQua.setStyle(STYLE_BTN_INACTIVE);
    }
  }

  // ====================================================================
  //                    NẠP NỘI DUNG VÀO SCROLLPANE
  // ====================================================================

  /**
   * Hiển thị danh sách thẻ auction (chế độ Bidder) vào FlowPane.
   * Được gọi bởi ResponseHandler khi nhận data từ Server.
   *
   * @param auctions Danh sách phiên đấu giá
   */
  public void loadFeedToUI(List<AuctionDTO> auctions) {
    this.currentAuctions = auctions;
    Platform.runLater(() -> {
      // Đảm bảo ScrollPane đang hiển thị FlowPane feedContainer
      mainContent.setContent(feedContainer);
      mainContent.setFitToWidth(true);
      mainContent.setFitToHeight(false);

      feedContainer.getChildren().clear();

      for (AuctionDTO auction : auctions) {
        try {
          FXMLLoader loader = new FXMLLoader(
              getClass().getResource("/com/auction/client/Bidder/AuctionItemCard.fxml"));
          Node cardNode = loader.load();

          AuctionItemCardController cardController = loader.getController();
          cardNode.setUserData(cardController);
          cardController.setData(auction, instance);

          feedContainer.getChildren().add(cardNode);
        } catch (IOException e) {
          System.err.println("Lỗi khi load Component thẻ sản phẩm: " + e.getMessage());
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Hiển thị danh sách thẻ auction (chế độ Seller) vào FlowPane.
   * Bao gồm card "Đăng bán sản phẩm" ở đầu danh sách.
   *
   * @param auctions Danh sách phiên đấu giá của seller
   */
  public void loadSellerFeedToUI(List<AuctionDTO> auctions) {
    this.currentAuctions = auctions;
    Platform.runLater(() -> {
      // Đảm bảo ScrollPane đang hiển thị FlowPane feedContainer
      mainContent.setContent(feedContainer);
      mainContent.setFitToWidth(true);
      mainContent.setFitToHeight(false);

      feedContainer.getChildren().clear();

      // === Card "Đăng bán sản phẩm" ===
      VBox addNewCard = new VBox(5);
      addNewCard.setPrefSize(760, 110);
      addNewCard.setAlignment(Pos.CENTER);

      String normalStyle = "-fx-background-color: #27ae60; -fx-border-color: #219653; "
          + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;";
      String hoverStyle = "-fx-background-color: #2ecc71; -fx-border-color: #27ae60; "
          + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand;";

      addNewCard.setStyle(normalStyle);
      addNewCard.setOnMouseEntered(e -> addNewCard.setStyle(hoverStyle));
      addNewCard.setOnMouseExited(e -> addNewCard.setStyle(normalStyle));

      Label plusIcon = new Label("+");
      plusIcon.setStyle("-fx-font-size: 60px; -fx-text-fill: white; -fx-font-weight: bold;");

      Label textLabel = new Label("Đăng bán sản phẩm");
      textLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

      addNewCard.getChildren().addAll(plusIcon, textLabel);
      addNewCard.setOnMouseClicked(event -> gotoUploadItem());

      feedContainer.getChildren().add(addNewCard);

      // === Các thẻ auction của seller ===
      for (AuctionDTO auction : auctions) {
        try {
          FXMLLoader loader = new FXMLLoader(
              getClass().getResource("/com/auction/client/Bidder/AuctionItemCard.fxml"));
          Node cardNode = loader.load();

          AuctionItemCardController cardController = loader.getController();
          cardController.setData(auction, instance);

          feedContainer.getChildren().add(cardNode);
        } catch (IOException e) {
          System.err.println("Lỗi khi load Component thẻ sản phẩm: " + e.getMessage());
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Hiển thị danh sách OrderCard (chế độ Result) vào FlowPane.
   * Được gọi bởi ResponseHandler khi nhận pending orders.
   *
   * @param orders Danh sách đơn hàng
   */
  public void loadOrdersToUI(List<OrderDTO> orders) {
    Platform.runLater(() -> {
      mainContent.setContent(feedContainer);
      mainContent.setFitToWidth(true);
      mainContent.setFitToHeight(false);

      feedContainer.getChildren().clear();

      for (OrderDTO order : orders) {
        try {
          FXMLLoader loader = new FXMLLoader(
              getClass().getResource("/com/auction/client/User/OrderCard.fxml"));
          Node cardNode = loader.load();

          OrderCardController cardController = loader.getController();
          cardNode.setUserData(cardController);
          cardController.setData(order, instance);

          feedContainer.getChildren().add(cardNode);
        } catch (IOException e) {
          System.err.println("Lỗi khi load Component OrderCard: " + e.getMessage());
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Nạp file FXML bất kì và thay thế toàn bộ nội dung ScrollPane.
   * Dùng cho Profile, Wallet, UploadItem, SellerRegisterForBidder, v.v.
   *
   * @param fxmlPath Đường dẫn tuyệt đối tới file FXML (bắt đầu bằng "/")
   */
  public void loadComponent(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent newNode = loader.load();

      mainContent.setContent(newNode);
      mainContent.setFitToHeight(true);
      mainContent.setFitToWidth(true);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // ====================================================================
  //                    CẬP NHẬT GIÁ REALTIME
  // ====================================================================

  /**
   * Cập nhật giá mới cho một phiên đấu giá trên giao diện.
   * Được gọi khi nhận broadcast giá mới từ Server.
   */
  public void updateAuctionPrice(String auctionId, BigDecimal newPrice) {
    for (AuctionDTO auction : currentAuctions) {
      if (auction.getAuctionId().equals(auctionId)) {
        auction.setCurrentPrice(newPrice);
        refreshAuctionCard(auctionId, newPrice);
        break;
      }
    }
  }

  private void refreshAuctionCard(String auctionId, BigDecimal newPrice) {
    Platform.runLater(() -> {
      for (Node node : feedContainer.getChildren()) {
        AuctionItemCardController controller =
            (AuctionItemCardController) node.getUserData();
        if (controller != null && controller.getAuctionId().equals(auctionId)) {
          controller.updatePrice(newPrice);
          break;
        }
      }
    });
  }

  // ====================================================================
  //                         THÔNG BÁO
  // ====================================================================

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
    loadHomeSidebar();
    resetFixedButtonStyles("home");

    // Reset ScrollPane về FlowPane feedContainer
    mainContent.setContent(feedContainer);
    mainContent.setFitToWidth(true);
    mainContent.setFitToHeight(false);

    // Request danh sách đấu giá mới nhất
    ServerConnection.sendData(new GetActiveAndWaitingAuctionsRequestDTO());
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
      loadSellerSidebar();
      resetFixedButtonStyles("seller");

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
    loadResultSidebar();
    resetFixedButtonStyles("result");

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
        ScreenController.primaryStage.setMaximized(false);
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
  //                   XỬ LÝ BUTTON SIDEBAR ĐỘNG
  // ====================================================================

  // --- Home context ---

  private void handleGetActiveAuctions() {
    System.out.println(">>> Đã bấm nút Đang diễn ra");
    ServerConnection.sendData(new GetActiveAuctionsRequestDTO());
  }

  private void handleGetWaitingAuctions() {
    ServerConnection.sendData(new GetWaitingAuctionsRequestDTO());
  }

  private void handleGetClosedAuctions() {
    ServerConnection.sendData(new GetClosedAuctionsRequestDTO());
  }

  // --- Seller context ---

  private void handleGetMyActiveAuctions() {
    // Reset về FlowPane feed trước khi load
    mainContent.setContent(feedContainer);
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetActiveAuctionsBySellerRequestDTO(userId));
  }

  private void handleGetPendingOrders() {
    String userId = SessionManager.getCurrentUser().getId();
    ServerConnection.sendData(new GetPendingOrdersOfSellerRequestDTO(userId));
  }

  private void handleGetCompletedOrders() {
    // TODO: implement khi server hỗ trợ
  }

  private void handleGetCanceledOrders() {
    // TODO: implement khi server hỗ trợ
  }

  // ====================================================================
  //                          GETTERS
  // ====================================================================

  /** Lấy ngữ cảnh hiện tại ("home", "seller", "result"). */
  public String getCurrentContext() {
    return currentContext;
  }
}
