package com.auction.client.screenhandler;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Lớp tiện ích hỗ trợ tạo bộ đếm ngược thời gian trong giao diện.
 * Cung cấp phương thức để thiết lập Timeline đếm ngược cho JavaFX Label.
 */
public class CountdownHelper {

    /**
     * Constructor private để ngăn việc khởi tạo lớp tiện ích.
     */
    private CountdownHelper() {
        // Ngăn khởi tạo lớp tiện ích
    }

    /**
     * Tạo và trả về một Timeline đếm ngược.
     *
     * @param timeLabel Label sẽ hiển thị thời gian nhảy.
     * @param endTime Thời điểm kết thúc.
     * @param onFinished Hành động sẽ thực thi khi đếm ngược về 0 (ví dụ: vô hiệu hóa nút bấm).
     */
    public static Timeline setupCountdown(javafx.scene.control.Label timeLabel, LocalDateTime endTime, Runnable onFinished){
        final Timeline[] timelineArr = new Timeline[1];
        // Tạo bộ đếm thời gian chạy mỗi 1 giây
        timelineArr[0] = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(endTime)) {
                timeLabel.setText("Đã kết thúc");
                timeLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                timelineArr[0].stop();
                // Thực thi hành động khi kết thúc (ví dụ: khóa nút bấm)
                if (onFinished != null) {
                    onFinished.run();
                }
            } else {
                String timeLeft = formatTimeLeft(now, endTime);
                timeLabel.setText("Thời gian còn lại: " + timeLeft);
            }
        }));

        timelineArr[0].setCycleCount(Animation.INDEFINITE);
        return timelineArr[0];
    }

    /**
     * Hàm tính toán và định dạng khoảng thời gian còn lại giữa hai mốc thời gian.
     * Trả về định dạng: ví dụ "02d 14:05:30" hoặc "14:05:30".
     */
    public static String formatTimeLeft(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return "00:00:00";
        }
        long days = ChronoUnit.DAYS.between(from, to);
        long hours = ChronoUnit.HOURS.between(from, to) % 24;
        long minutes = ChronoUnit.MINUTES.between(from, to) % 60;
        long seconds = ChronoUnit.SECONDS.between(from, to) % 60;

        if (days > 0) {
            return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    /**
     * Định dạng Duration của đơn hàng sang ngôn ngữ tự nhiên tiếng Việt.
     * Trả về định dạng: "d ngày h giờ m phút" hoặc "h giờ m phút s giây" hoặc "m phút s giây".
     */
    public static String formatDuration(java.time.Duration duration) {
        if (duration == null) {
            return "";
        }
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (days > 0) {
            return String.format("%d ngày %d giờ %d phút", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d giờ %d phút %d giây", hours, minutes, seconds);
        } else {
            return String.format("%d phút %d giây", minutes, seconds);
        }
    }
}
