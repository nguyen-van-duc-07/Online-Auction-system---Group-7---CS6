package com.auction.client.screenhandler;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;

public class UploadProductController {
  @FXML
  private TextField nameProductField;
  @FXML
  private TextField iniPriceField;
  @FXML
  private TextField minStepPriceField;
  @FXML
  private DatePicker finishDateField;
  @FXML
  private TextArea descriptionField;
  @FXML
  private ImageView imageField;

  private File selectedImageFile;

  HomeController homeController = new HomeController();

  @FXML
  public void gotoHomeWithHyperLink() {
    ScreenController.switchScreen("Hone.fxml", "Trang chủ");
  }

  @FXML
  public void gotoResult() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Bạn chưa lưu thông tin sản phẩm",
        "Bạn có chắc chắn muốn chuyển sang trang Kết quả đấu giá không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        homeController.gotoResult();
      }
    });
  }

  @FXML
  public void gotoProfile() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Bạn chưa lưu thông tin sản phẩm",
        "Bạn có chắc chắn muốn chuyển sang trang Thông tin tài khoản không?").ifPresent(Response -> {
      if (Response == ButtonType.OK) {
        homeController.gotoProfile();
      }
    });
  }

  @FXML
  public void gotoLogin() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Bạn chưa lưu thông tin sản phẩm",
        "Bạn có chắc chắn muốn đăng xuất không?").ifPresent(response -> {
          if (response == ButtonType.OK) {
            homeController.gotoLogin();
          }
    });
  }

  @FXML
  public void gotoWallet() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Bạn chưa lưu thông tin sản phẩm",
        "Bạn có chắc chắn muốn chuyển sang trang Ví người dùng không?").ifPresent(Response -> {
          if (Response == ButtonType.OK) {
            homeController.gotoWallet();
          }
    });
  }

  @FXML
  public void gotoSellerHome() {
    ScreenController.showAlert(Alert.AlertType.CONFIRMATION, "Bạn chưa lưu thông tin sản phẩm",
        "Bạn có chắc chắn muốn chuyển sang trang Quản lý sản phẩm không?").ifPresent(Response -> {
          if (Response == ButtonType.OK) {
            homeController.gotoSellerHome();
          }
    });
  }

  @FXML
  public void uploadProduct() {
    String nameProduct = nameProductField.getText();
    String iniPrice = iniPriceField.getText();
    String minStepPrice = minStepPriceField.getText();
    LocalDate finishDate = finishDateField.getValue();
    String description = descriptionField.getText();
  }

  @FXML
  public void handleChooseImage() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Chọn ảnh sản phẩm đấu giá");
     FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
         "Image Files", "*.jpg", "*.png", "*.jpeg");
     fileChooser.getExtensionFilters().add(imageFilter);

     // Mở cửa sổ lên và "đứng chờ" người dùng chọn file
     File file = fileChooser.showOpenDialog(ScreenController.primaryStage);

     if (file != null) {
       // Lưu trữ ảnh vừa chọn vào trong selectedImageFile để tí nữa upload lên khi người dùng ấn upload
       selectedImageFile = file;

       // Ép đường dẫn file thành dạng chuỗi URI để JavaFX hiểu
       Image image = new Image(file.toURI().toString());

       // Hiển thị ảnh lên phần khung trên màn hình
       imageField.setImage(image);
     }
  }
}
