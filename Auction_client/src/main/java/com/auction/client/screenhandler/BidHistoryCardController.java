package com.auction.client.screenhandler;

import com.auction.client.network.SessionManager;
import com.auction.client.util.CurrencyUtils;
import com.auction.shared.model.transaction.BidTransaction;
import com.auction.shared.model.user.UserDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Bộ điều khiển (Controller) cho thẻ hiển thị lịch sử đặt giá (BidHistoryCard).
 * Hiển thị thông tin lượt đặt giá của người chơi bao gồm số tiền, thời gian đặt và huy hiệu xếp hạng.
 */
public class BidHistoryCardController {

  @FXML
  private HBox cardRoot;
  @FXML
  private StackPane avatarContainer;
  @FXML
  private Label lblAvatar;
  @FXML
  private Label lblBidderName;
  @FXML
  private Label lblBidTime;
  @FXML
  private Label lblBidAmount;
  @FXML
  private Label lblBadge;

  /**
   * Bơm dữ liệu giao dịch đặt giá để hiển thị lên thẻ lịch sử đặt giá.
   *
   * @param tx thông tin giao dịch đặt giá (BidTransaction)
   * @param isLeading đánh dấu xem giao dịch này có đang dẫn đầu (giá cao nhất) hay không
   * @param sequence số thứ tự hiển thị của thẻ trong danh sách lịch sử đặt giá
   */
  public void setData(BidTransaction tx, boolean isLeading, int sequence) {
    // 1. Định dạng tiền tệ
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
    symbols.setGroupingSeparator('.');
    lblBidAmount.setText(CurrencyUtils.formatD(tx.getBidAmount()));

    // 2. Định dạng thời gian
    if (tx.getCreatedAt() != null) {
      lblBidTime.setText(tx.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")));
    } else {
      lblBidTime.setText("N/A");
    }

    // 3. Tên người chơi và xử lý che tên (Masking) / Hiển thị "Bạn" nếu khớp với current user
    String bidderId = tx.getBidderId();
    UserDTO currentUser = SessionManager.getCurrentUser();
    String displayName = bidderId;

    if (currentUser != null && bidderId != null) {
      if (bidderId.equals(currentUser.getId())) {
        displayName = "Bạn (" + maskId(bidderId) + ")";
        lblAvatar.setText("⭐");
        avatarContainer.setStyle("-fx-background-color: #FEF3C7; -fx-background-radius: 50;"); // Màu vàng cam ấm
        lblAvatar.setStyle("-fx-text-fill: #D97706;");
      } else {
        displayName = "Người chơi (" + maskId(bidderId) + ")";
        lblAvatar.setText(getInitials(bidderId));
        avatarContainer.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 50;"); // Mặc định xám nhạt
        lblAvatar.setStyle("-fx-text-fill: #64748B;");
      }
    } else if (bidderId != null) {
      displayName = "Người chơi (" + maskId(bidderId) + ")";
      lblAvatar.setText(getInitials(bidderId));
    }
    lblBidderName.setText(displayName);

    // 4. Huy hiệu (Badge) và phân cấp màu sắc cho card
    if (isLeading) {
      // Card dẫn đầu: Nền xanh nhạt, viền xanh lá, huy hiệu vương miện nổi bật
      cardRoot.setStyle("-fx-background-color: #ECFDF5; -fx-background-radius: 12; -fx-border-color: #A7F3D0; -fx-border-width: 1.5; -fx-border-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(16, 185, 129, 0.1), 10, 0, 0, 4);");
      lblBidAmount.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 14.0;");
      lblBadge.setText("👑 Dẫn đầu");
      lblBadge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 3 8; -fx-background-radius: 6; -fx-font-weight: bold; -fx-font-size: 10px;");
    } else {
      // Card thông thường
      cardRoot.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-border-color: #F1F5F9; -fx-border-width: 1; -fx-border-radius: 12;");
      lblBidAmount.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 14.0;");
      lblBadge.setText("#" + sequence);
      lblBadge.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-padding: 3 8; -fx-background-radius: 6; -fx-font-weight: bold; -fx-font-size: 10px;");
    }
  }

  private String maskId(String id) {
    if (id == null || id.isEmpty()) return "N/A";
    if (id.length() <= 3) return id;
    return id.substring(0, 3) + "***";
  }

  private String getInitials(String id) {
    if (id == null || id.isEmpty()) return "👤";
    return id.substring(0, 1).toUpperCase();
  }
}
