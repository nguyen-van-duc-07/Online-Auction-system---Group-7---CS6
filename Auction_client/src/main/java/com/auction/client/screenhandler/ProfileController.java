package com.auction.client.screenhandler;

import com.auction.client.network.SessionManager;
import com.auction.client.screenhandler.admin.AdminScreenController;
import com.auction.shared.enums.UserRole;
import com.auction.shared.model.user.UserDTO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.stage.FileChooser;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Bộ điều khiển (Controller) cho màn hình thông tin tài khoản người dùng (Profile).
 * Hiển thị các thông tin chi tiết cá nhân như họ tên, địa chỉ, ngày sinh, email và số điện thoại.
 */
public class ProfileController implements Initializable {
  private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

  @FXML
  private Circle avatarCircle;
  @FXML
  private Label accountNameLabel;
  @FXML
  private Label addressLabel;
  @FXML
  private Label dobLabel;
  @FXML
  private Label emailLabel;
  @FXML
  private Label phoneNumberLabel;

  /**
   * Hàm được tự động gọi khi màn hình Profile.fxml được nạp lên giao diện.
   * Truy xuất dữ liệu từ {@link SessionManager} và điền tự động vào các nhãn hiển thị.
   *
   * @param location vị trí đường dẫn tương đối của đối tượng gốc
   * @param resources tài nguyên sử dụng để bản địa hóa đối tượng gốc
   */
  public void initialize(URL location, ResourceBundle resources) {
    UserDTO currentUser = SessionManager.getCurrentUser();

    if (currentUser != null) {
      if (currentUser.getAccountName() != null) {
        if (currentUser.getAccountName() != null) {
          accountNameLabel.setText(currentUser.getAccountName());
        }
        if (currentUser.getEmail() != null) {
          emailLabel.setText(currentUser.getEmail());
        }
        if (currentUser.getPhoneNumber() != null) {
          phoneNumberLabel.setText(currentUser.getPhoneNumber());
        }
        if (currentUser.getDob() != null) {
          LocalDate dob = currentUser.getDob();
          String dobString = dob.getDayOfMonth() + " Tháng " + dob.getMonthValue() + " Năm " + dob.getYear();
          dobLabel.setText(dobString);
        }
        if (currentUser.getAddress() != null) {
          addressLabel.setText(currentUser.getAddress());
        }
      }
      loadAvatar();
    }
  }

  private void loadAvatar() {
    UserDTO currentUser = SessionManager.getCurrentUser();
    if (currentUser == null) return;

    String userId = currentUser.getId();
    File avatarFolder = new File("uploads/avatars");
    if (!avatarFolder.exists()) {
      avatarFolder.mkdirs();
    }

    File avatarFile = new File(avatarFolder, userId + ".png");
    if (avatarFile.exists()) {
      try {
        Image image = new Image(avatarFile.toURI().toString());
        avatarCircle.setFill(new ImagePattern(image));
        return;
      } catch (Exception e) {
        log.error("Lỗi khi nạp ảnh avatar cục bộ: ", e);
      }
    }

    // Nếu không có ảnh cục bộ, thử nạp ảnh mặc định từ internet, nếu lỗi thì dùng gradient
    try {
      Image defaultImage = new Image("https://avatar.iran.liara.run/public/boy", true);
      avatarCircle.setFill(new ImagePattern(defaultImage));
    } catch (Exception e) {
      log.warn("Không thể tải ảnh avatar mặc định từ internet, chuyển sang dùng gradient: ", e);
      avatarCircle.setFill(new javafx.scene.paint.LinearGradient(
          0, 0, 1, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
          new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#2BA659")),
          new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#54ca85"))
      ));
    }
  }

  /**
   * Xử lý khi chọn avatar mới.
   */
  @FXML
  public void handleChooseAvatar() {
    UserDTO currentUser = SessionManager.getCurrentUser();
    if (currentUser == null) {
      ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi truy cập", "Vui lòng đăng nhập!");
      return;
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Chọn ảnh đại diện của bạn");
    FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
        "Image Files", "*.jpg", "*.png", "*.jpeg");
    fileChooser.getExtensionFilters().add(imageFilter);

    File file = fileChooser.showOpenDialog(avatarCircle.getScene().getWindow());

    if (file != null) {
      if (file.length() > 5 * 1024 * 1024) {
        ScreenController.showAlert(Alert.AlertType.WARNING, "Cảnh báo",
            "Kích thước ảnh quá lớn. Vui lòng chọn ảnh dưới 5MB!");
        return;
      }

      try {
        File avatarFolder = new File("uploads/avatars");
        if (!avatarFolder.exists()) {
          avatarFolder.mkdirs();
        }

        File destFile = new File(avatarFolder, currentUser.getId() + ".png");
        java.nio.file.Files.copy(file.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        Image image = new Image(destFile.toURI().toString());
        avatarCircle.setFill(new ImagePattern(image));

        ScreenController.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật ảnh đại diện thành công!");
      } catch (Exception e) {
        log.error("Lỗi khi cập nhật ảnh đại diện: ", e);
        ScreenController.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu ảnh đại diện!");
      }
    }
  }

  /**
   * Xem ảnh đại diện phóng to dưới dạng Modal cao cấp.
   */
  @FXML
  public void handleViewFullAvatar() {
    UserDTO currentUser = SessionManager.getCurrentUser();
    if (currentUser == null) return;

    File avatarFile = new File("uploads/avatars/" + currentUser.getId() + ".png");
    Image imageToView = null;
    if (avatarFile.exists()) {
      try {
        imageToView = new Image(avatarFile.toURI().toString());
      } catch (Exception e) {
        log.error("Lỗi khi nạp ảnh đại diện để xem: ", e);
      }
    }

    if (imageToView == null) {
      try {
        imageToView = new Image("https://avatar.iran.liara.run/public/boy");
      } catch (Exception e) {
        // Fallback offline
      }
    }

    if (imageToView != null && !imageToView.isError()) {
      javafx.stage.Stage dialog = new javafx.stage.Stage();
      dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
      dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
      dialog.setTitle("Ảnh đại diện");

      ImageView imageView = new ImageView(imageToView);
      imageView.setFitWidth(350.0);
      imageView.setFitHeight(350.0);
      imageView.setPreserveRatio(true);

      // Bo tròn hoàn hảo cho ảnh hiển thị
      Circle clip = new Circle(175.0, 175.0, 175.0);
      imageView.setClip(clip);

      imageView.setEffect(new javafx.scene.effect.DropShadow(30.0, javafx.scene.paint.Color.BLACK));

      VBox container = new VBox(20.0);
      container.setAlignment(javafx.geometry.Pos.CENTER);
      container.setPadding(new javafx.geometry.Insets(30.0));
      container.setStyle("-fx-background-color: rgba(26, 32, 44, 0.9); -fx-background-radius: 25; -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 25; -fx-border-width: 1.5;");

      Label nameLabel = new Label(currentUser.getAccountName());
      nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20; -fx-font-weight: bold;");

      Button closeBtn = new Button("Đóng");
      closeBtn.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #2D3748; -fx-background-radius: 20; -fx-font-weight: bold; -fx-padding: 8 24; -fx-cursor: hand;");
      closeBtn.setOnAction(event -> dialog.close());

      container.setOnKeyPressed(event -> {
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
          dialog.close();
        }
      });

      container.getChildren().addAll(imageView, nameLabel, closeBtn);

      javafx.scene.Scene scene = new javafx.scene.Scene(container);
      scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
      dialog.setScene(scene);
      dialog.showAndWait();
    } else {
      ScreenController.showAlert(Alert.AlertType.WARNING, "Thông tin", "Hiện tại chưa có ảnh đại diện nào được cập nhật.");
    }
  }

  /**
   * Chuyển hướng người dùng sang giao diện chỉnh sửa thông tin cá nhân.
   */
  @FXML
  public void gotoEditProfile() {
    AdminScreenController adminController = AdminScreenController.getInstance();
    if (adminController != null) {
      adminController.loadComponent("/com/auction/client/User/EditProfile.fxml");
    } else {
      MainLayoutController mainLayoutController = MainLayoutController.getInstance();
      if (mainLayoutController != null) {
        mainLayoutController.loadComponent("/com/auction/client/User/EditProfile.fxml");
      }
    }
  }
}
