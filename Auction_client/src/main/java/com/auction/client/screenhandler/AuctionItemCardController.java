package com.auction.client.screenhandler;
import com.auction.shared.enums.AuctionStatusUI;
import com.auction.shared.response.AuctionResponseDTO;
import javafx.animation.Animation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDateTime;

public class AuctionItemCardController {

    @FXML private Label nameLabel;
    @FXML private Label statusLabel;
    @FXML private Label priceLabel;
    @FXML private Label timeLabel;
    @FXML private Button bidButton;
    private Timeline countdownTimer;
    /**
     * Hàm này dùng để bơm dữ liệu từ HomeController truyền sang.
     */
    public void setData(AuctionResponseDTO auction, HomeController parentController) {
        // 1. Đổ dữ liệu text
        nameLabel.setText(auction.getItem().getName());

        AuctionStatusUI status = AuctionStatusUI.fromShared(auction.getStatus());
        statusLabel.setText(status.getDisplayName());
        statusLabel.setStyle("-fx-text-fill: " + status.getColor() + "; -fx-font-weight: bold;");

        String formattedPrice = String.format("%,.0f VNĐ", auction.getCurrentHighestPrice());
        priceLabel.setText(formattedPrice);

        // Tạo bộ đếm thời gian chạy mỗi 1 giây
        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = auction.getEndTime();

            if (now.isAfter(endTime)) {
                timeLabel.setText("Đã kết thúc");
                timeLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                countdownTimer.stop();
            } else {
                // Tính toán khoảng cách giữa hiện tại và lúc kết thúc
                long days = ChronoUnit.DAYS.between(now, endTime);
                long hours = ChronoUnit.HOURS.between(now, endTime) % 24;
                long minutes = ChronoUnit.MINUTES.between(now, endTime) % 60;
                long seconds = ChronoUnit.SECONDS.between(now, endTime) % 60;

                // Định dạng hiển thị: ví dụ "02d 14:05:30" hoặc "14:05:30"
                String timeLeft;
                if (days > 0) {
                    timeLeft = String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
                } else {
                    timeLeft = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                }

                timeLabel.setText(timeLeft);
            }
        }));

        countdownTimer.setCycleCount(Animation.INDEFINITE);
        countdownTimer.play();

        // 2. Gắn sự kiện cho nút bấm
        // Gọi ngược lại hàm chuyển trang của HomeController
        bidButton.setOnAction(e -> parentController.gotoProductDetail(auction));
    }
}