package com.auction.client.screenhandler;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CountdownHelperTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Đã khởi tạo trước đó, có thể bỏ qua
        }
    }

    @Test
    void setupCountdown_taoTimelineThanhCong() {
        Label label = new Label();
        LocalDateTime endTime = LocalDateTime.now().plusHours(2);

        Timeline timeline = CountdownHelper.setupCountdown(label, endTime, null);

        assertNotNull(timeline);
        assertEquals(Timeline.INDEFINITE, timeline.getCycleCount());
    }

    @Test
    void formatTimeLeft_lonHonMotNgay_traVeDinhDangNgayGio() {
        LocalDateTime from = LocalDateTime.of(2026, 5, 28, 12, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 5, 30, 14, 5, 10); // Cách 2 ngày, 2 giờ, 5 phút, 10 giây

        String result = CountdownHelper.formatTimeLeft(from, to);

        assertEquals("2d 02:05:10", result);
    }

    @Test
    void formatTimeLeft_nhoHonMotNgay_traVeDinhDangGioPhutGiay() {
        LocalDateTime from = LocalDateTime.of(2026, 5, 28, 12, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 5, 28, 18, 5, 10); // Cách 6 giờ, 5 phút, 10 giây

        String result = CountdownHelper.formatTimeLeft(from, to);

        assertEquals("06:05:10", result);
    }

    @Test
    void formatTimeLeft_null_traVeDinhDangMacDinh() {
        assertEquals("00:00:00", CountdownHelper.formatTimeLeft(null, LocalDateTime.now()));
        assertEquals("00:00:00", CountdownHelper.formatTimeLeft(LocalDateTime.now(), null));
    }

    @Test
    void formatDuration_lonHonMotNgay_traVeDinhDangNgayGioPhutTiengViet() {
        // 2 ngày, 3 giờ, 4 phút, 10 giây
        Duration duration = Duration.ofDays(2).plusHours(3).plusMinutes(4).plusSeconds(10);

        String result = CountdownHelper.formatDuration(duration);

        assertEquals("2 ngày 3 giờ 4 phút", result);
    }

    @Test
    void formatDuration_duoiMotNgayTrenMotGio_traVeDinhDangGioPhutGiayTiengViet() {
        // 3 giờ, 4 phút, 5 giây
        Duration duration = Duration.ofHours(3).plusMinutes(4).plusSeconds(5);

        String result = CountdownHelper.formatDuration(duration);

        assertEquals("3 giờ 4 phút 5 giây", result);
    }

    @Test
    void formatDuration_duoiMotGio_traVeDinhDangPhutGiayTiengViet() {
        // 4 phút, 5 giây
        Duration duration = Duration.ofMinutes(4).plusSeconds(5);

        String result = CountdownHelper.formatDuration(duration);

        assertEquals("4 phút 5 giây", result);
    }

    @Test
    void formatDuration_null_traVeChuoiRong() {
        assertEquals("", CountdownHelper.formatDuration(null));
    }
}
