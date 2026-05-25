package com.auction.client.screenhandler;

import com.auction.shared.enums.ItemType;
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
  private TextField startHourField;
  @FXML
  private TextField startMinuteField;
  @FXML
  private DatePicker finishDateField;
  @FXML
  private TextField finishHourField;
  @FXML
  private TextField finishMinuteField;
  @FXML
  private TextField durationField;
  @FXML
  private ComboBox<String> categoryField;
  @FXML
  private VBox dynamicAttributesBox;
  @FXML
  private TextArea descriptionField;

  @FXML
  public void initialize() {
    categoryField.getItems().addAll(
        ItemType.ELECTRONICS.getValue(),
        ItemType.VEHICLES.getValue(),
        ItemType.COLLECTIBLES.getValue(),
        ItemType.FASHION.getValue(),
        ItemType.SPORTS.getValue(),
        ItemType.ARTS.getValue(),
        ItemType.OTHER.getValue()
    );
  }

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
      startHourField.setText(String.format("%02d", auction.getStartTime().getHour()));
      startMinuteField.setText(String.format("%02d", auction.getStartTime().getMinute()));
    }

    if (auction.getEndTime() != null) {
      finishDateField.setValue(auction.getEndTime().toLocalDate());
      finishHourField.setText(String.format("%02d", auction.getEndTime().getHour()));
      finishMinuteField.setText(String.format("%02d", auction.getEndTime().getMinute()));
    }

    if (auction.getStartTime() != null && auction.getEndTime() != null) {
      Duration duration = Duration.between(auction.getStartTime(), auction.getEndTime());
      long totalMinutes = duration.toMinutes();
      double decimalHours = totalMinutes / 60.0;

      long days = totalMinutes / (24 * 60);
      long hours = (totalMinutes % (24 * 60)) / 60;
      long minutes = totalMinutes % 60;

      StringBuilder humanReadable = new StringBuilder();
      if (days > 0) {
        humanReadable.append(days).append(" ngày ");
      }
      if (hours > 0) {
        humanReadable.append(hours).append(" giờ ");
      }
      if (minutes > 0) {
        humanReadable.append(minutes).append(" phút");
      }

      String durationText;
      if (humanReadable.length() > 0) {
        durationText = String.format("%.1f giờ (%s)", decimalHours, humanReadable.toString().trim());
      } else {
        durationText = String.format("%.1f giờ", decimalHours);
      }
      durationField.setText(durationText);
    }

    if (auction.getImagePath() != null && !auction.getImagePath().isEmpty() && imageField != null) {
      String imageUrl = "http://" + NetworkConfig.DEFAULT_HOST + ":"
          + NetworkConfig.IMAGE_SERVER_PORT + "/images/" + auction.getImagePath();
      Image image = new Image(imageUrl, true); // true = load nền để không đơ UI
      imageField.setImage(image);
    }
  }
}
