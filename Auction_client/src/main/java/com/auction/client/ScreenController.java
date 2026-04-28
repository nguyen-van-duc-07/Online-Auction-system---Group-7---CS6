package com.auction.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class ScreenController
{
    // Dùng để chuyển sang trang bất kì hiệu quả hơn
    public static void switchScreen(ActionEvent event, String fxmlFile, String title)
    {
        try
        {
            Parent root = FXMLLoader.load(ScreenController.class.getResource(fxmlFile));
            Scene scene = new Scene(root);
            Stage stage = null;
            if (event != null)
            {
                if (event.getSource() instanceof Node) // Nếu phần vừa ấn là thể hiện của Button là lớp con của Node
                {
                    stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                } else if (event.getSource() instanceof MenuItem) // Nếu phần vừa ấn là thể hiện của MenuItem
                {
                    stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
                }
            }
            if (stage == null)
            {
                stage = new Stage();
            }
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình: " + fxmlFile, event);
        }
    }

    // Dùng để tạo ra cảnh báo
    public static Optional<ButtonType> showAlert(Alert.AlertType type, String title, String content, ActionEvent event)
    {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        Stage stage = null;
        if (event != null)
        {
            if (event.getSource() instanceof Node)
            {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            }
            else if (event.getSource() instanceof MenuItem)
            {
                stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
            }
            if (stage != null)
            {
                alert.initOwner(stage);
            }
        }
        return alert.showAndWait();
    }

    // Dùng để tạo ra cửa sổ con mới, thiết lập quan hệ cha con, khi cửa sổ cha đóng thì cửa sổ con cũng đóng theo
    public static void creatSubWindow(ActionEvent event, String  fxmlFile, String title)
    {
        try
        {
            Parent root = FXMLLoader.load(ScreenController.class.getResource(fxmlFile));
            Scene scene = new Scene(root);
            // Tạo cửa sổ con mới
            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setScene(scene);

            // Xác đinh cửa sổ cha
            if (event == null)
            {
                showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể mở cửa sổ con", null);
            }
            Stage ownerStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Xác định mối quan hệ cha con giữa 2 cửa sổ
            newStage.initOwner(ownerStage);
            // Ngăn người dùng tương tác với cửa sổ cũ khi cửa sổ mới đang mở (tùy chọn)
            newStage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            // Tính toán tọa độ X, Y
            // Công thức: Tọa độ gốc của cha + (Nửa chiều rộng cha - Nửa chiều rộng con)
            double x = ownerStage.getX() + (ownerStage.getWidth() / 2) - (root.prefWidth(-1) / 2);
            double y = ownerStage.getY() + (ownerStage.getHeight() / 2) - (root.prefHeight(-1) / 2);
            newStage.setX(x);
            newStage.setY(y);

            newStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình" + fxmlFile, event);
        }
    }
}