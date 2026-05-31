package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.client.service.InvoiceService;
import com.auction.shared.enums.OrderStatus;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.model.transaction.PrizedTransaction;
import com.auction.shared.model.user.InfoDTO;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.CancelOrderRequestDTO;
import com.auction.shared.request.ConfirmOrderRequestDTO;
import com.auction.shared.util.CurrencyUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Bộ điều khiển (Controller) cho giao diện thanh toán đơn hàng (PaymentScreen).
 * Quản lý thông tin giao hàng, hiển thị số tiền thanh toán còn lại sau khi khấu trừ đặt cọc
 * và thực hiện thanh toán qua ví của người dùng.
 */
public class PaymentScreenController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(PaymentScreenController.class);

  // --- 1. Shipping Information ---
  @FXML
  private TextField txtFullName;
  @FXML
  private TextField txtPhoneNumber;
  @FXML
  private TextField txtAddress;

  // --- 3. Order Summary ---
  @FXML
  private Label lblItemName;
  @FXML
  private Label lblOrderId;
  @FXML
  private Label lblFinalPrice;
  @FXML
  private Label lblDepositAmount;
  @FXML
  private Label lblTotalAmount;

  // --- 4. Buttons ---
  @FXML
  private Button btnCompletePayment;
  @FXML
  private Button btnCancelOrder;
  @FXML
  private Button btnBack;
  private BigDecimal itemFinalPrice;
  private BigDecimal totalAmountToPay;
  private String currentUserId;
  private String currentAuctionId;
  private String currentOrderId;
  private boolean isCanceling = false;

  /**
   * Cửa sổ thanh toán đang hoạt động.
   */
  public static PaymentScreenController instance;

  /**
   * Khởi tạo bộ điều khiển thanh toán đơn hàng.
   *
   * @param location vị trí đường dẫn tương đối của đối tượng gốc
   * @param resources tài nguyên sử dụng để bản địa hóa đối tượng gốc
   */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;

    btnCompletePayment.setOnAction(event -> handlePayment());
    btnCancelOrder.setOnAction(event -> handleCancel());
    btnBack.setOnAction(event -> handleBack());
  }

  /**
   * Thiết lập và nạp dữ liệu chi tiết của đơn hàng lên giao diện thanh toán.
   *
   * @param order đối tượng chứa dữ liệu đơn hàng
   */
  public void setOrderData(Order order) {
    this.currentUserId = order.getBuyerId();
    this.currentAuctionId = order.getAuctionId();
    this.totalAmountToPay = order.getRemainingAmount();
    this.currentOrderId = order.getId();
    this.itemFinalPrice = order.getFinalPrice();

    DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
    symbols.setGroupingSeparator('.');
    lblItemName.setText(order.getItemName() != null ? order.getItemName() : "Sản phẩm");
    lblOrderId.setText("Mã đơn hàng: " + order.getId());
    lblFinalPrice.setText(CurrencyUtils.formatD(order.getFinalPrice()));
    lblDepositAmount.setText(CurrencyUtils.formatD(order.getDepositAmount().negate()));
    lblTotalAmount.setText(CurrencyUtils.formatD(order.getRemainingAmount()));
    UserDTO currentUser = SessionManager.getCurrentUser();
    boolean isBuyerAndPending = order.getStatus() == OrderStatus.PENDING
        && order.getBuyerId().equals(currentUser.getId());
    if (isBuyerAndPending) {
      txtFullName.setText(order.getConsigneeName() != null ? order.getConsigneeName() : currentUser.getAccountName());
      txtPhoneNumber.setText(order.getPhoneNumber() != null ? order.getPhoneNumber() : currentUser.getPhoneNumber());
      txtAddress.setText(order.getAddress() != null ? order.getAddress() : currentUser.getAddress());
    } else {
      txtFullName.setText(order.getConsigneeName() != null ? order.getConsigneeName() : "");
      txtPhoneNumber.setText(order.getPhoneNumber() != null ? order.getPhoneNumber() : "");
      txtAddress.setText(order.getAddress() != null ? order.getAddress() : "");
      disableAllForViewOnly();
    }
  }
  private void disableAllForViewOnly() {
    txtFullName.setDisable(true);
    txtFullName.setEditable(false);
    txtPhoneNumber.setDisable(true);
    txtPhoneNumber.setEditable(false);
    txtAddress.setDisable(true);
    txtAddress.setEditable(false);

    btnCompletePayment.setDisable(true);
    btnCompletePayment.setText("HÓA ĐƠN CHỈ XEM 📄");
    btnCompletePayment.setStyle("-fx-background-color: #64748B; -fx-background-radius: 8; -fx-cursor: default;");

    btnCancelOrder.setDisable(true);
    btnCancelOrder.setVisible(false); // Ẩn hoàn toàn nút hủy đơn hàng
  }

  private void handlePayment() {
    isCanceling = false;
    String consigneeName = txtFullName.getText() != null ? txtFullName.getText().trim() : "";
    String phoneNumber = txtPhoneNumber.getText() != null ? txtPhoneNumber.getText().trim() : "";
    String address = txtAddress.getText() != null ? txtAddress.getText().trim() : "";
    if (consigneeName.isEmpty()
        || phoneNumber.isEmpty()
        || address.isEmpty()) {
      showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ thông tin giao hàng!");
      return;
    }
    InfoDTO buyerInfo = new InfoDTO(consigneeName, phoneNumber, address);

    // Khóa nút bấm trên UI để tránh click nhiều lần
    btnCompletePayment.setDisable(true);
    btnCompletePayment.setText("Đang xử lý...");
    btnCancelOrder.setDisable(true);

    // Lấy mã đơn hàng từ SessionManager
    String orderId = SessionManager.getCurrentOrderId();
    if (orderId == null || orderId.isEmpty()) {
      orderId = currentAuctionId;
    }

    // Gửi ConfirmOrderRequestDTO lên Server
    ConfirmOrderRequestDTO requestDTO = new ConfirmOrderRequestDTO(orderId, buyerInfo);
    ServerConnection.sendData(requestDTO);
  }

  @FXML
  private void handleCancel() {
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Xác nhận hủy đơn");
    confirmAlert.setHeaderText(null);
    confirmAlert.setContentText("Bạn có chắc chắn muốn hủy thanh toán cho đơn hàng này không?");

    ButtonType btnYes = new ButtonType("Có, hủy đơn", ButtonBar.ButtonData.YES);
    ButtonType btnNo = new ButtonType("Không, quay lại", ButtonBar.ButtonData.NO);
    confirmAlert.getButtonTypes().setAll(btnYes, btnNo);

    Optional<ButtonType> result = confirmAlert.showAndWait();

    if (result.isPresent() && result.get() == btnYes) {
      isCanceling = true;
      btnCancelOrder.setDisable(true);
      btnCancelOrder.setText("Đang hủy...");
      btnCompletePayment.setDisable(true);

      String orderId = SessionManager.getCurrentOrderId();
      if (orderId == null || orderId.isEmpty()) {
        orderId = currentAuctionId;
      }

      // Gửi CancelOrderRequestDTO lên Server
      CancelOrderRequestDTO requestDTO = new CancelOrderRequestDTO(orderId);
      ServerConnection.sendData(requestDTO);
    }
  }

  // Callback khi Server xác nhận thanh toán/hủy đơn hàng thành công
  /**
   * Xử lý sự kiện khi máy chủ xác nhận thực hiện thành công giao dịch liên quan đến đơn hàng.
   * Xuất hóa đơn PDF và đóng cửa sổ giao dịch.
   *
   * @param message thông điệp phản hồi từ máy chủ
   */
  public void onOrderActionSuccess(String message) {
    Platform.runLater(() -> {
      if (isCanceling) {
        log.info("[THANH_TOAN] Nhận thông báo xác nhận hủy đơn từ Server. Không xuất hóa đơn.");
      } else {
        log.info("[THANH_TOAN] Nhận thông báo xác nhận thanh toán từ Server. Khởi chạy tiến trình tạo hóa đơn...");
        try {
          String buyerId = currentUserId != null && !currentUserId.trim().isEmpty() ? currentUserId : (SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : "Unknown");
          String validAuctionId = currentAuctionId != null && !currentAuctionId.trim().isEmpty() ? currentAuctionId : "N/A";
          String validOrderId = currentOrderId != null && !currentOrderId.trim().isEmpty() ? currentOrderId : "N/A";

          BigDecimal finalPrice = itemFinalPrice != null ? itemFinalPrice : BigDecimal.ZERO;
          if (finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            finalPrice = BigDecimal.ZERO; // fallback to avoid PrizedTransaction validation failure
          }
          String consigneeName = txtFullName.getText() != null
              && !txtFullName.getText().trim().isEmpty() ? txtFullName.getText().trim() : "N/A";
          String phoneNumber = txtPhoneNumber.getText() != null
              && !txtPhoneNumber.getText().trim().isEmpty() ? txtPhoneNumber.getText().trim() : "N/A";
          String address = txtAddress.getText() != null
              && !txtAddress.getText().trim().isEmpty() ? txtAddress.getText().trim() : "N/A";
          String itemName = lblItemName.getText() != null
              && !lblItemName.getText().trim().isEmpty() ? lblItemName.getText().trim() : "SP";;

          Order order = new Order();
          order.setId(validOrderId);
          order.setAuctionId(validAuctionId);
          order.setBuyerId(buyerId);
          order.setItemName(itemName);
          order.setConsigneeName(consigneeName);
          order.setPhoneNumber(phoneNumber);
          order.setAddress(address);
          order.setFinalPrice(finalPrice);
          exportAndOpenInvoice(order);
        } catch (Exception e) {
          log.error("[THANH_TOAN] Gặp lỗi nghiêm trọng khi chuẩn bị dữ liệu hóa đơn", e);
        }
      }
      handleBack();
    });
  }

  // Callback khi Server báo thất bại
  /**
   * Xử lý khi yêu cầu giao dịch liên quan đến đơn hàng thất bại.
   *
   * @param message thông điệp phản hồi từ máy chủ
   */
  public void onOrderActionFailed(String message) {
    Platform.runLater(() -> {
      btnCompletePayment.setDisable(false);
      btnCompletePayment.setText("HOÀN TẤT THANH TOÁN");
      btnCancelOrder.setDisable(false);
      btnCancelOrder.setText("Hủy đơn hàng");
      isCanceling = false;
    });
  }

  // Quay lại trang trước đó
  @FXML
  private void handleBack() {
    log.info("Quay lại màn hình trước...");
    ScreenController.goBack();
  }

  // --- HÀM TIỆN ÍCH ---

  private void exportAndOpenInvoice(Order order) {

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Lưu Hóa Đơn PDF");
    fileChooser.setInitialFileName("HoaDon_" + order.getAuctionId() + ".pdf");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

    Stage stage = (Stage) btnCompletePayment.getScene().getWindow();
    File fileToSave = fileChooser.showSaveDialog(stage);

    if (fileToSave != null) {
      String filePath = fileToSave.getAbsolutePath();
      InvoiceService invoiceService = new InvoiceService();
      
      // Tính toán giá tạm tính và phí vận chuyển
      BigDecimal shippingFee = BigDecimal.ZERO;
      BigDecimal finalTotal = itemFinalPrice != null ? itemFinalPrice : BigDecimal.ZERO;
      
      // Gọi service xuất PDF với đầy đủ dữ liệu
      boolean exportSuccess = invoiceService.exportInvoiceToPdf(
          order,
          shippingFee,
          finalTotal,
          filePath
      );

      if (exportSuccess) {
        log.info("[HOA_DON] Xuất hóa đơn PDF THÀNH CÔNG cho phiên: {} tại đường dẫn: {}", order.getAuctionId(), filePath);
        try {
          if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop.getDesktop().open(fileToSave);
          }
        } catch (IOException e) {
          log.error("[HOA_DON] Không thể tự động mở file PDF sau khi xuất", e);
        }
      } else {
        log.error("[HOA_DON] Xuất hóa đơn PDF THẤT BẠI cho phiên: {} tại đường dẫn: {}", order.getAuctionId(), filePath);
        showAlert(Alert.AlertType.ERROR, "Lỗi xuất hóa đơn", "Có lỗi xảy ra trong quá trình khởi tạo hóa đơn PDF!");
      }
    }
  }

  private void showAlert(Alert.AlertType alertType, String title, String content) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}
