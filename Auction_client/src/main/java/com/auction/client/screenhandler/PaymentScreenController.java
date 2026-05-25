package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.client.network.SessionManager;
import com.auction.shared.model.transaction.PrizedTransaction;
import com.auction.client.service.InvoiceService;
import com.auction.shared.model.order.Order;
import com.auction.shared.model.order.OrderDTO;
import com.auction.shared.model.user.InfoDTO;
import com.auction.shared.model.user.UserDTO;
import com.auction.shared.request.ConfirmOrderRequestDTO;
import com.auction.shared.request.CancelOrderRequestDTO;
import com.auction.shared.response.AuctionResultDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class PaymentScreenController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(PaymentScreenController.class);

  // --- 1. Shipping Information ---
  @FXML
  private TextField txtFullName;
  @FXML
  private TextField txtPhoneNumber;
  @FXML
  private TextField txtAddress;

  // --- 2. Payment Methods ---
  @FXML
  private RadioButton rbUserWallet;
  @FXML
  private RadioButton rbBankTransfer;
  @FXML
  private RadioButton rbCashOnDelivery;
  private ToggleGroup paymentMethodGroup;

  // --- 3. Order Summary ---
  @FXML
  private Label lblItemName;
  @FXML
  private Label lblItemId;
  @FXML
  private Label lblItemPrice;
  @FXML
  private Label lblSubTotal;
  @FXML
  private Label lblShippingFee;
  @FXML
  private Label lblDiscount;
  @FXML
  private Label lblTotalAmount;

  // --- 4. Buttons ---
  @FXML
  private Button btnCompletePayment;
  @FXML
  private Button btnCancelOrder;
  @FXML
  private Button btnBack;

  private BigDecimal totalAmountToPay;
  private String currentUserId;
  private String currentAuctionId;
  private String currentItemId;
  public static PaymentScreenController instance;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instance = this;
    paymentMethodGroup = new ToggleGroup();
    rbUserWallet.setToggleGroup(paymentMethodGroup);
    rbBankTransfer.setToggleGroup(paymentMethodGroup);
    rbCashOnDelivery.setToggleGroup(paymentMethodGroup);

    rbUserWallet.setSelected(true);

    btnCompletePayment.setOnAction(event -> handlePayment());
    btnCancelOrder.setOnAction(event -> handleCancel());
    btnBack.setOnAction(event -> handleBack());

    // Tự động điền (Pre-populate) thông tin giao hàng của người dùng hiện tại từ Profile
    UserDTO currentUser = SessionManager.getCurrentUser();
    if (currentUser != null) {
      if (currentUser.getAccountName() != null && !currentUser.getAccountName().trim().isEmpty()) {
        txtFullName.setText(currentUser.getAccountName());
      }
      if (currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().trim().isEmpty()) {
        txtPhoneNumber.setText(currentUser.getPhoneNumber());
      }
      if (currentUser.getAddress() != null && !currentUser.getAddress().trim().isEmpty()) {
        txtAddress.setText(currentUser.getAddress());
      }
    }
  }

  // Nạp dữ liệu khi kết thúc phiên trực tiếp
  public void setOrderData(AuctionResultDTO resultDTO) {
    this.currentUserId = resultDTO.getWinnerId();
    this.currentAuctionId = resultDTO.getAuctionId();
    this.currentItemId = resultDTO.getItemId();

    BigDecimal shippingFee = new BigDecimal("30000.00");
    this.totalAmountToPay = resultDTO.getFinalPrice().add(shippingFee);

    DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
    symbols.setGroupingSeparator('.');
    DecimalFormat currencyFormat = new DecimalFormat("#,###", symbols);

    lblItemName.setText(resultDTO.getItemName());
    lblItemId.setText("Mã sản phẩm: " + resultDTO.getItemId());
    lblItemPrice.setText(currencyFormat.format(resultDTO.getFinalPrice()) + "đ");
    lblSubTotal.setText(currencyFormat.format(resultDTO.getFinalPrice()) + "đ");
    lblShippingFee.setText(currencyFormat.format(shippingFee) + "đ");
    lblDiscount.setText("-0đ");
    lblTotalAmount.setText(currencyFormat.format(totalAmountToPay) + "đ");
  }

  // Nạp dữ liệu khi mở từ thông báo (chứa đối tượng Order và item name phong phú từ Server)
  public void setOrderData(Order order, String itemName, String itemId) {
    this.currentUserId = order.getBuyerId();
    this.currentAuctionId = order.getAuctionId();
    this.currentItemId = itemId != null ? itemId : order.getAuctionId();

    BigDecimal shippingFee = new BigDecimal("30000.00");
    this.totalAmountToPay = order.getFinalPrice().add(shippingFee);

    DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
    symbols.setGroupingSeparator('.');
    DecimalFormat currencyFormat = new DecimalFormat("#,###", symbols);

    lblItemName.setText(itemName != null ? itemName : "Sản phẩm");
    lblItemId.setText("Mã đơn hàng: " + order.getId());
    lblItemPrice.setText(currencyFormat.format(order.getFinalPrice()) + "đ");
    lblSubTotal.setText(currencyFormat.format(order.getFinalPrice()) + "đ");
    lblShippingFee.setText(currencyFormat.format(shippingFee) + "đ");
    lblDiscount.setText("-0đ");
    lblTotalAmount.setText(currencyFormat.format(totalAmountToPay) + "đ");

    // Sử dụng thông tin giao hàng lưu trong Order nếu có
    if (order.getConsigneeName() != null && !order.getConsigneeName().trim().isEmpty()) {
      txtFullName.setText(order.getConsigneeName());
    }
    if (order.getPhoneNumber() != null && !order.getPhoneNumber().trim().isEmpty()) {
      txtPhoneNumber.setText(order.getPhoneNumber());
    }
    if (order.getAddress() != null && !order.getAddress().trim().isEmpty()) {
      txtAddress.setText(order.getAddress());
    }
  }

  // Nạp dữ liệu khi mở từ thẻ Đơn hàng (Order Card)
  public void setOrderData(OrderDTO orderDTO) {
    this.currentUserId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : "Unknown";
    this.currentAuctionId = orderDTO.getAuctionId();
    this.currentItemId = orderDTO.getAuctionId(); // fallback

    BigDecimal shippingFee = new BigDecimal("30000.00");
    this.totalAmountToPay = orderDTO.getFinalPrice().add(shippingFee);

    DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
    symbols.setGroupingSeparator('.');
    DecimalFormat currencyFormat = new DecimalFormat("#,###", symbols);

    lblItemName.setText(orderDTO.getItemName());
    lblItemId.setText("Mã đơn hàng: " + orderDTO.getOrderId());
    lblItemPrice.setText(currencyFormat.format(orderDTO.getFinalPrice()) + "đ");
    lblSubTotal.setText(currencyFormat.format(orderDTO.getFinalPrice()) + "đ");
    lblShippingFee.setText(currencyFormat.format(shippingFee) + "đ");
    lblDiscount.setText("-0đ");
    lblTotalAmount.setText(currencyFormat.format(totalAmountToPay) + "đ");
  }

  private void handlePayment() {
    String consigneeName = txtFullName.getText().trim();
    String phoneNumber = txtPhoneNumber.getText().trim();
    String address = txtAddress.getText().trim();
    if (consigneeName.isEmpty() ||
        phoneNumber.isEmpty() ||
        address.isEmpty()) {
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
  public void onOrderActionSuccess(String message) {
    Platform.runLater(() -> {
      log.info("[THANH_TOAN] Nhận thông báo xác nhận thanh toán từ Server. Khởi chạy tiến trình tạo hóa đơn...");
      try {
        String buyerId = currentUserId != null && !currentUserId.trim().isEmpty() ? currentUserId : (SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : "Unknown");
        String validAuctionId = currentAuctionId != null && !currentAuctionId.trim().isEmpty() ? currentAuctionId : "Mã đơn hàng";
        String validItemId = currentItemId != null && !currentItemId.trim().isEmpty() ? currentItemId : validAuctionId;
        
        BigDecimal shippingFee = new BigDecimal("30000.00");
        BigDecimal finalPrice = totalAmountToPay != null ? totalAmountToPay.subtract(shippingFee) : BigDecimal.ZERO;
        if (finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
          finalPrice = new BigDecimal("1.00"); // fallback to avoid PrizedTransaction validation failure
        }

        // Tự động khởi tạo PrizedTransaction từ thông tin đang hiển thị để in hóa đơn PDF
        PrizedTransaction transaction = new PrizedTransaction(
            buyerId,
            "Hệ thống",
            validAuctionId,
            validItemId,
            finalPrice
        );
        exportAndOpenInvoice(transaction);
      } catch (Exception e) {
        log.error("[THANH_TOAN] Gặp lỗi nghiêm trọng khi chuẩn bị dữ liệu hóa đơn", e);
      }
      handleBack();
    });
  }

  // Callback khi Server báo thất bại
  public void onOrderActionFailed(String message) {
    Platform.runLater(() -> {
      btnCompletePayment.setDisable(false);
      btnCompletePayment.setText("HOÀN TẤT THANH TOÁN");
      btnCancelOrder.setDisable(false);
      btnCancelOrder.setText("Hủy đơn hàng");
    });
  }

  // Quay lại trang trước đó
  @FXML
  private void handleBack() {
    log.info("Quay lại màn hình trước...");
    ScreenController.goBack();
  }

  // --- HÀM TIỆN ÍCH ---

  private void exportAndOpenInvoice(PrizedTransaction transaction) {
    // Bảo vệ phòng ngừa nếu transaction bị null hoặc chứa giá trị rỗng gây lỗi
    String auctionId = (transaction != null && transaction.getAuctionId() != null) ? transaction.getAuctionId() : (currentAuctionId != null ? currentAuctionId : "Mã đơn hàng");
    String itemId = (transaction != null && transaction.getItemId() != null && !transaction.getItemId().trim().isEmpty()) ? transaction.getItemId() : (currentItemId != null && !currentItemId.trim().isEmpty() ? currentItemId : auctionId);
    BigDecimal price = (transaction != null && transaction.getFinalPrice() != null) ? transaction.getFinalPrice() : (totalAmountToPay != null ? totalAmountToPay.subtract(new BigDecimal("30000.00")) : BigDecimal.ZERO);
    if (price.compareTo(BigDecimal.ZERO) < 0) {
      price = BigDecimal.ZERO;
    }
    
    // Tạo transaction an toàn dự phòng để gửi vào service
    PrizedTransaction safeTransaction = transaction;
    if (safeTransaction == null) {
      try {
        safeTransaction = new PrizedTransaction(
            currentUserId != null ? currentUserId : "Unknown",
            "Hệ thống",
            auctionId,
            itemId,
            price.compareTo(BigDecimal.ZERO) > 0 ? price : new BigDecimal("1.00")
        );
      } catch (Exception e) {
        log.error("Lỗi khi tạo safeTransaction", e);
      }
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Lưu Hóa Đơn PDF");
    fileChooser.setInitialFileName("HoaDon_" + auctionId + ".pdf");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

    Stage stage = (Stage) btnCompletePayment.getScene().getWindow();
    File fileToSave = fileChooser.showSaveDialog(stage);

    if (fileToSave != null) {
      String filePath = fileToSave.getAbsolutePath();
      InvoiceService invoiceService = new InvoiceService();
      
      // Tính toán giá tạm tính và phí vận chuyển
      BigDecimal shippingFee = new BigDecimal("30000.00");
      BigDecimal finalTotal = totalAmountToPay != null ? totalAmountToPay : price.add(shippingFee);
      BigDecimal finalPrice = finalTotal.subtract(shippingFee);
      if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
        finalPrice = BigDecimal.ZERO;
      }
      
      // Thu thập thông tin giao nhận thực tế từ UI
      String consigneeName = txtFullName.getText().trim();
      String phoneNumber = txtPhoneNumber.getText().trim();
      String address = txtAddress.getText().trim();
      String itemName = lblItemName.getText().trim();
      
      if (consigneeName.isEmpty()) consigneeName = "N/A";
      if (phoneNumber.isEmpty()) phoneNumber = "N/A";
      if (address.isEmpty()) address = "N/A";
      if (itemName.isEmpty()) itemName = "Sản phẩm đấu giá";
      
      // Gọi service xuất PDF với đầy đủ dữ liệu
      boolean exportSuccess = invoiceService.exportInvoiceToPdf(
          safeTransaction,
          consigneeName,
          phoneNumber,
          address,
          itemName,
          finalPrice,
          shippingFee,
          finalTotal,
          filePath
      );

      if (exportSuccess) {
        log.info("[HOA_DON] Xuất hóa đơn PDF THÀNH CÔNG cho phiên: {} tại đường dẫn: {}", auctionId, filePath);
        try {
          if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop.getDesktop().open(fileToSave);
          }
        } catch (IOException e) {
          log.error("[HOA_DON] Không thể tự động mở file PDF sau khi xuất", e);
        }
      } else {
        log.error("[HOA_DON] Xuất hóa đơn PDF THẤT BẠI cho phiên: {} tại đường dẫn: {}", auctionId, filePath);
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

  public void processPaymentResponse(boolean isSuccess, String message, PrizedTransaction transaction) {
    Platform.runLater(() -> {
      if (isSuccess) {
        showAlert(Alert.AlertType.INFORMATION, "Thành công",
            "Thanh toán thành công!\nHệ thống sẽ xuất hóa đơn cho bạn.");
        exportAndOpenInvoice(transaction);
        handleBack();
      } else {
        showAlert(Alert.AlertType.ERROR, "Thanh toán thất bại", message);
        btnCompletePayment.setDisable(false);
        btnCompletePayment.setText("HOÀN TẤT THANH TOÁN");
      }
    });
  }
}