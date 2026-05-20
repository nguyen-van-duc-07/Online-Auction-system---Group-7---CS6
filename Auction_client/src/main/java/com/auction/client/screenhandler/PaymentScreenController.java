package com.auction.client.screenhandler;

import com.auction.client.network.ServerConnection;
import com.auction.shared.model.transaction.PrizedTransaction;
import com.auction.client.service.InvoiceService;
import com.auction.shared.request.PaymentRequestDTO;
import com.auction.shared.response.AuctionResultDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.Optional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ResourceBundle;

public class PaymentScreenController implements Initializable {
    // --- 1. Shipping Information ---
    @FXML private TextField txtFullName;
    @FXML private TextField txtPhoneNumber;
    @FXML private TextField txtAddress;

    // --- 2. Payment Methods ---
    @FXML private RadioButton rbUserWallet;
    @FXML private RadioButton rbBankTransfer;
    @FXML private RadioButton rbCashOnDelivery;
    private ToggleGroup paymentMethodGroup;

    // --- 3. Order Summary ---
    @FXML private Label lblItemName;
    @FXML private Label lblItemId;
    @FXML private Label lblItemPrice;
    @FXML private Label lblSubTotal;
    @FXML private Label lblShippingFee;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotalAmount;

    // --- 4. Buttons ---
    @FXML private Button btnCompletePayment;
    @FXML private Button btnCancelOrder;
    @FXML private Button btnBack;

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
    }

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

    private void handlePayment() {
        if (txtFullName.getText().trim().isEmpty() ||
                txtPhoneNumber.getText().trim().isEmpty() ||
                txtAddress.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ thông tin giao hàng!");
            return;
        }

        RadioButton selectedMethod = (RadioButton) paymentMethodGroup.getSelectedToggle();
        String paymentMethodName = selectedMethod.getText();

        // Khóa nút bấm trên UI
        btnCompletePayment.setDisable(true);
        btnCompletePayment.setText("Đang xử lý...");

        // Đóng gói RequestDTO và gửi đi qua Socket
        PaymentRequestDTO requestDTO = PaymentRequestDTO.builder()
                .userId(currentUserId)
                .auctionId(currentAuctionId)
                .itemId(currentItemId)
                .amount(totalAmountToPay)
                .paymentMethod(paymentMethodName)
                .build();

        ServerConnection.sendData(requestDTO);
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

                // Mở khóa lại nút bấm cho người dùng thử lại
                btnCompletePayment.setDisable(false);
                btnCompletePayment.setText("HOÀN TẤT THANH TOÁN");
            }
        });
    }

    @FXML
    private void handleCancel() {
        // 1. Tạo hộp thoại xác nhận (Confirmation Dialog)
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận hủy đơn");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Bạn có chắc chắn muốn hủy thanh toán cho đơn hàng này không?");

        // Thay đổi text của 2 nút mặc định (OK / Cancel) sang tiếng Việt cho thân thiện
        ButtonType btnYes = new ButtonType("Có, hủy đơn", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("Không, quay lại", ButtonBar.ButtonData.NO);
        confirmAlert.getButtonTypes().setAll(btnYes, btnNo);

        // 2. Hiển thị hộp thoại và chờ người dùng click (showAndWait)
        Optional<ButtonType> result = confirmAlert.showAndWait();

        // 3. Xử lý logic dựa trên nút người dùng chọn
        if (result.isPresent() && result.get() == btnYes) {
            // Nếu người dùng chọn "Có"
            System.out.println("Đã ghi nhận hủy đơn hàng lên hệ thống!");

            // Hiện thông báo hoàn tất hủy và quay về màn hình trước
            showAlert(Alert.AlertType.INFORMATION, "Đã hủy", "Đơn hàng của bạn đã được hủy thành công!");
            handleBack();
        } else {
            // Nếu người dùng chọn "Không" hoặc bấm dấu X đóng cửa sổ
            // Không làm gì cả, giữ nguyên họ ở lại màn hình thanh toán
            System.out.println("Người dùng đã hủy thao tác, tiếp tục thanh toán.");
        }
    }

    private void handleBack() {
        System.out.println("Quay lại màn hình trước...");
        ScreenController.switchScreen("Bidder/ItemAuction.fxml", "Đấu giá sản phẩm");
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
                System.err.println("Không thể tự động mở file PDF: " + e.getMessage());
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