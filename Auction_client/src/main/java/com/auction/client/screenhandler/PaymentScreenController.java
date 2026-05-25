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

    // Kiểm tra vai trò và trạng thái
    boolean isSeller = false;
    UserDTO currentUser = SessionManager.getCurrentUser();
    if (currentUser != null && order.getSellerId() != null) {
      isSeller = currentUser.getId().equals(order.getSellerId());
    }

    if (isSeller || order.getStatus() != com.auction.shared.enums.OrderStatus.PENDING) {
      disableAllForViewOnly();
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

    // Đổ dữ liệu thông tin giao hàng có sẵn từ orderDTO (nếu có)
    if (orderDTO.getConsigneeName() != null && !orderDTO.getConsigneeName().trim().isEmpty()) {
      txtFullName.setText(orderDTO.getConsigneeName());
    }
    if (orderDTO.getPhoneNumber() != null && !orderDTO.getPhoneNumber().trim().isEmpty()) {
      txtPhoneNumber.setText(orderDTO.getPhoneNumber());
    }
    if (orderDTO.getAddress() != null && !orderDTO.getAddress().trim().isEmpty()) {
      txtAddress.setText(orderDTO.getAddress());
    }

    // Kiểm tra vai trò của người dùng hiện tại đối với đơn hàng này
    boolean isSeller = false;
    UserDTO currentUser = SessionManager.getCurrentUser();
    if (currentUser != null && orderDTO.getSellerId() != null) {
      isSeller = currentUser.getId().equals(orderDTO.getSellerId());
    }

    if (isSeller || orderDTO.getStatus() != com.auction.shared.enums.OrderStatus.PENDING) {
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

    rbUserWallet.setDisable(true);
    rbBankTransfer.setDisable(true);
    rbCashOnDelivery.setDisable(true);

    btnCompletePayment.setDisable(true);
    btnCompletePayment.setText("HÓA ĐƠN CHỈ XEM 📄");
    btnCompletePayment.setStyle("-fx-background-color: #64748B; -fx-background-radius: 8; -fx-cursor: default;");

    btnCancelOrder.setDisable(true);
    btnCancelOrder.setVisible(false); // Ẩn hoàn toàn nút hủy đơn hàng
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
      try {
        // Tự động khởi tạo PrizedTransaction từ thông tin đang hiển thị để in hóa đơn PDF
        PrizedTransaction transaction = new PrizedTransaction(
            currentUserId != null ? currentUserId : (SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : "Unknown"),
            "Hệ thống",
            currentAuctionId != null ? currentAuctionId : "Mã đơn hàng",
            currentItemId != null ? currentItemId : "Mã SP",
            totalAmountToPay != null ? totalAmountToPay : BigDecimal.ZERO
        );
        exportAndOpenInvoice(transaction);
      } catch (Exception e) {
        log.error("Không thể xuất hóa đơn", e);
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
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Lưu Hóa Đơn PDF");
    fileChooser.setInitialFileName("HoaDon_" + transaction.getAuctionId() + ".pdf");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

    Stage stage = (Stage) btnCompletePayment.getScene().getWindow();
    File fileToSave = fileChooser.showSaveDialog(stage);

    if (fileToSave != null) {
      String filePath = fileToSave.getAbsolutePath();
      InvoiceService invoiceService = new InvoiceService();
      invoiceService.exportInvoiceToPdf(transaction, filePath);

      try {
        if (java.awt.Desktop.isDesktopSupported()) {
          java.awt.Desktop.getDesktop().open(fileToSave);
        }
      } catch (IOException e) {
        log.error("Không thể tự động mở file PDF", e);
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