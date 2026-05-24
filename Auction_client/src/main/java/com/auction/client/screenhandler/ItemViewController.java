package com.auction.client.screenhandler;

import com.auction.shared.model.item.ItemDTO;
import com.auction.shared.network.NetworkConfig;
import com.auction.shared.response.AuctionResponseDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.util.Map;

/**
 * Controller cho giao diện xem thông tin chi tiết sản phẩm (ItemView.fxml).
 */
public class ItemViewController {

  @FXML
  private ImageView imageField;
  @FXML
  private TextField nameItemField;
  @FXML
  private DatePicker startDateField;
  @FXML
  private ComboBox<String> startHourField;
  @FXML
  private ComboBox<String> startMinuteField;
  @FXML
  private DatePicker finishDateField;
  @FXML
  private ComboBox<String> finishHourField;
  @FXML
  private ComboBox<String> finishMinuteField;
  @FXML
  private TextField durationField;
  @FXML
  private ComboBox<String> categoryField;
  @FXML
  private VBox dynamicAttributesBox;
  @FXML
  private TextArea descriptionField;

  /**
   * Khởi tạo dữ liệu để điền vào các trường trên giao diện.
   *
   * @param auction Đối tượng chứa thông tin phiên đấu giá từ Server
   */
  public void initData(AuctionResponseDTO auction) {
    if (auction == null) return;

    ItemDTO item = auction.getItem();
    if (item != null) {
      nameItemField.setText(item.getName() != null ? item.getName() : "");
      descriptionField.setText(item.getDescription() != null ? item.getDescription() : "");

      if (item.getType() != null) {
        categoryField.setValue(item.getType().getValue());
      }

      dynamicAttributesBox.getChildren().clear();
      if (item.getAdditionalAttributes() != null) {
        for (Map.Entry<String, String> entry : item.getAdditionalAttributes().entrySet()) {
          VBox vBox = new VBox(6.0);
          Label label = new Label(entry.getKey());
          TextField textField = new TextField(entry.getValue());
          textField.setEditable(false);
          textField.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-background-color: #F8FAFC;");
          vBox.getChildren().addAll(label, textField);
          dynamicAttributesBox.getChildren().add(vBox);
        }
      }
    }

    if (auction.getStartTime() != null) {
      startDateField.setValue(auction.getStartTime().toLocalDate());
      startHourField.setValue(String.format("%02d", auction.getStartTime().getHour()));
      startMinuteField.setValue(String.format("%02d", auction.getStartTime().getMinute()));
    }

    if (auction.getEndTime() != null) {
      finishDateField.setValue(auction.getEndTime().toLocalDate());
      finishHourField.setValue(String.format("%02d", auction.getEndTime().getHour()));
      finishMinuteField.setValue(String.format("%02d", auction.getEndTime().getMinute()));
    }

    if (auction.getStartTime() != null && auction.getEndTime() != null) {
      long hours = Duration.between(auction.getStartTime(), auction.getEndTime()).toHours();
      durationField.setText(String.valueOf(hours));
    }

    if (auction.getImagePath() != null && !auction.getImagePath().isEmpty() && imageField != null) {
      String imageUrl = "http://" + NetworkConfig.DEFAULT_HOST + ":"
          + NetworkConfig.IMAGE_SERVER_PORT + "/images/" + auction.getImagePath();
      Image image = new Image(imageUrl, true); // true = load nền để không đơ UI
      imageField.setImage(image);
    }
  }
}
