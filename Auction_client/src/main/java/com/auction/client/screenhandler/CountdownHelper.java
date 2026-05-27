package com.auction.client.screenhandler;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.awt.*;
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

                timeLabel.setText("Thời gian còn lại: " + timeLeft);
            }
        }));

        timelineArr[0].setCycleCount(Animation.INDEFINITE);
        return timelineArr[0];
    }
}
