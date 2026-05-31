package com.auction.client.screenhandler;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Cung cấp thông báo Toast dạng popup động trượt từ trên xuống trong ứng dụng.
 * Thông báo sẽ tự động ẩn đi sau 8 giây hoặc khi người dùng nhấp chuột vào.
 */
public class ToastNotification {

  /**
   * Constructor private để ngăn khởi tạo đối tượng của lớp tiện ích thông báo.
   */
  private ToastNotification() {
    // Ngăn khởi tạo lớp tiện ích
  }

  /**
   * Hiển thị thông báo Toast trượt từ trên xuống ở góc trên bên phải màn hình.
   *
   * @param owner cửa sổ chính sở hữu thông báo này (Stage)
   * @param title tiêu đề của thông báo Toast
   * @param message nội dung chi tiết của thông báo
   * @param onClick hành động sẽ thực thi khi người dùng click vào thông báo (có thể null)
   */
  public static void show(Stage owner, String title, String message, Runnable onClick) {
    Popup popup = new Popup();

    // Container chính
    HBox container = new HBox(12);
    container.setAlignment(Pos.CENTER_LEFT);
    container.setPadding(new Insets(14, 18, 14, 18));
    container.setStyle(
        "-fx-background-color: #1a1a2e;"
            + "-fx-background-radius: 14;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 4);"
    );
    container.setPrefWidth(340);

    // Icon
    Label icon = new Label("🏆");
    icon.setStyle("-fx-font-size: 28;");

    // Text container
    VBox textBox = new VBox(4);
    Label titleLabel = new Label(title);
    titleLabel.setStyle(
        "-fx-text-fill: #f0c040;"
            + "-fx-font-weight: bold;"
            + "-fx-font-size: 14;"
    );
    Label messageLabel = new Label(message);
    messageLabel.setStyle(
        "-fx-text-fill: #e0e0e0;"
            + "-fx-font-size: 12;"
    );
    messageLabel.setWrapText(true);
    messageLabel.setMaxWidth(250);

    textBox.getChildren().addAll(titleLabel, messageLabel);
    container.getChildren().addAll(icon, textBox);

    // Nhấn vào thì chạy callback
    container.setOnMouseClicked(e -> {
      popup.hide();
      if (onClick != null) onClick.run();
    });

    // Hover effect
    container.setOnMouseEntered(e ->
        container.setStyle(
            "-fx-background-color: #16213e;"
                + "-fx-background-radius: 14;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 6);"
                + "-fx-cursor: hand;"
        )
    );
    container.setOnMouseExited(e ->
        container.setStyle(
            "-fx-background-color: #1a1a2e;"
                + "-fx-background-radius: 14;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 4);"
        )
    );

    popup.getContent().add(container);
    popup.setAutoHide(false);

    // Hiện popup ở góc trên bên phải
    double screenWidth = owner.getX() + owner.getWidth();
    double startX = screenWidth - 360;
    double startY = owner.getY() - 80; // bắt đầu từ trên
    double endY = owner.getY() + 20;   // trượt xuống vị trí cuối

    popup.show(owner, startX, startY);

    // Animation trượt xuống
    DoubleProperty y = new SimpleDoubleProperty(startY);

    // Đồng bộ property với popup
    y.addListener((obs, oldVal, newVal) ->
        popup.setAnchorY(newVal.doubleValue())
    );

    // Animation hiện lên
    Timeline slideIn = new Timeline(
        new KeyFrame(Duration.ZERO,
            new KeyValue(y, startY, Interpolator.EASE_OUT)
        ),
        new KeyFrame(Duration.millis(400),
            new KeyValue(y, endY, Interpolator.EASE_OUT)
        )
    );

    // Tự động ẩn sau 8 giây
    Timeline autoHide = new Timeline(
        new KeyFrame(Duration.seconds(8), e -> {

          Timeline slideOut = new Timeline(
              new KeyFrame(Duration.ZERO,
                  new KeyValue(y, endY, Interpolator.EASE_IN)
              ),
              new KeyFrame(Duration.millis(300),
                  new KeyValue(y, startY, Interpolator.EASE_IN)
              )
          );

          slideOut.setOnFinished(ev -> popup.hide());
          slideOut.play();
        })
    );

    slideIn.play();
    autoHide.play();
  }
}
